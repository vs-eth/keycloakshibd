/*
 * Offers e-mail based Shibboleth authentication via HTTP Headers set by an Apache proxy.
 */
package org.vseth.auth.keycloak.shibd;

import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import java.util.Optional;

public class ShibbolethAuthenticator implements Authenticator {
    public static final String CREDENTIAL_TYPE = "shibboleth";
    private static final Logger logger = Logger.getLogger(ShibbolethAuthenticator.class);

    @Override
    public boolean requiresUser() {
        return false;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
    }

    @Override
    public void authenticate(final AuthenticationFlowContext context) {
        logger.debug("Got authentication request");

        Optional<String> email = context.getHttpRequest().getHttpHeaders().getRequestHeader("mail").stream().findAny();
        if (!email.isPresent()) {
            logger.error("No email header passed. This should not happen!");
            context.failure(AuthenticationFlowError.INTERNAL_ERROR);
            return;
        }
        logger.debug("Got mail: " + email.get());

        Optional<String> persistentId = context.getHttpRequest().getHttpHeaders().getRequestHeader("persistent-id").stream().findAny();
        if (!persistentId.isPresent()) {
            logger.error("No persistent-id header passed. This should not happen!");
            context.failure(AuthenticationFlowError.INTERNAL_ERROR);
            return;
        }
        logger.debug("Got persistent id: " + persistentId.get());

        // Try fetching the user by E-Mail address.
        // This field is indexed and thus querying it is fast. Assuming a user's E-Mail doesn't change often,
        // even external users can be authenticated quickly like this.
        UserModel user = context.getSession().users().getUserByEmail(email.get(), context.getRealm());
        if (user != null) {
            if (user.getAttribute("persistent-id").stream().noneMatch(s -> s.equals(persistentId.get()))) {
                // This case shouldn't happen because in this case there are accounts with identical E-Mails and
                // two different persistent IDs (which CAN be the case with eduID). In this case we just set whatever
                // persistent ID we are currently seeing - we do not prefer one ID over the other.
                logger.warn("The E-Mail of the currently authenticated user (E-Mail: " + email.get() +
                        ", persistent-id: " + persistentId.get() + ") is already taken by a different user (username " +
                        user.getUsername() + ", persistent-id: " + user.getFirstAttribute("persistent-id") +
                        ")! These users" + "will be merged now.");
            }
            logger.debug("Found user by E-Mail: " + user.getUsername());
            user.setSingleAttribute("persistent-id", persistentId.get());
            context.setUser(user);
            context.success();
        } else {
            // If we can't find the user by E-Mail we check if there exists a user with the corresponding persistent
            // id. This is potentially very slow, thus we first check if the provided E-Mail address is already
            // known to us.
            Optional<UserModel> existingUser = context.getSession().users().getUsers(context.getRealm()).stream()
                    .filter(u -> u.getAttribute("persistent-id").stream().anyMatch(s -> s.equals(persistentId.get())))
                    .findAny();
            if (existingUser.isPresent()) {
                logger.debug("Found existing user by persistent ID: " + existingUser.get().getUsername());
                logger.info("Re-setting E-Mail for user " + persistentId.get());
                existingUser.get().setEmail(email.get());
                context.setUser(existingUser.get());
                context.success();
            } else {
                logger.info("Encountered new user, creating new profile");
                String firstName = context.getHttpRequest().getHttpHeaders().getRequestHeader("givenName").get(0);
                String lastName = context.getHttpRequest().getHttpHeaders().getRequestHeader("surname").get(0);

                // If the E-Mail address is <name>@<domain>, construct the username as:
                // ext-user-<name>-<domain>
                String username = "ext-user-" + email.get().replace("@", "-");
                UserModel newUser = context.getSession().userStorageManager().addUser(context.getRealm(), username);
                newUser.setEmail(email.get());
                newUser.setFirstName(firstName);
                newUser.setLastName(lastName);
                newUser.setSingleAttribute("persistent-id", persistentId.get());
                newUser.setEnabled(true);
                newUser.setEmailVerified(true);

                logger.info("successfully created user");
                context.setUser(newUser);
                context.success();
            }
        }
    }

    @Override
    public void action(AuthenticationFlowContext context) {
    }

    @Override
    public void close() {
    }
}