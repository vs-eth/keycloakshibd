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
        logger.info("Got authentication request:");
        context.getHttpRequest().getHttpHeaders().getRequestHeaders().entrySet().forEach(x -> logger.info("  " + x.getKey() + ": " + x.getValue()));

        Optional<String> email = context.getHttpRequest().getHttpHeaders().getRequestHeader("mail").stream().findFirst();
        if (!email.isPresent()) {
            logger.error("No email passed! This should not happen.");
        }
        logger.info("Got mail: " + email);

        UserModel user = context.getSession().users().getUserByEmail(email.get(), context.getRealm());
        if (user != null) {
            logger.info("Found user: " + user.getUsername());
            context.setUser(user);
            context.success();
        } else {
            Optional<String> persistentId = context.getHttpRequest().getHttpHeaders().getRequestHeader("persistent-id").stream().findFirst();
            if (!persistentId.isPresent()) {
                logger.warn("Header `persistent-id` was not passed. This should not happen!");
                context.failure(AuthenticationFlowError.INTERNAL_ERROR);
            }
            Optional<UserModel> existingUser = context.getSession().users().getUsers(context.getRealm()).stream().filter(u -> u.getFirstAttribute("persistent-id").equals(persistentId.get())).findFirst();
            if (existingUser.isPresent()) {
                logger.info("Found existing user: " + existingUser.get().getUsername());
                context.setUser(existingUser.get());
                context.success();
            } else {
                logger.info("Encountered new user, creating new profile");
                // create user
                //UserModel newUser = context.getSession().userStorageManager().addUser(context.getRealm(), persistentId.get());
                context.failure(AuthenticationFlowError.UNKNOWN_USER);
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