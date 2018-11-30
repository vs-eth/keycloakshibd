package org.vseth.auth.keycloak.shibd;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import java.util.List;
import java.util.Optional;

public class ShibbolethAuthenticator implements Authenticator {
    public static final String CREDENTIAL_TYPE = "shibboleth";

    @Override
    public boolean requiresUser() {
        return false;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) { }

    @Override
    public void authenticate(final AuthenticationFlowContext context) {
        List<String> emails = context.getHttpRequest().getHttpHeaders().getRequestHeader("mail");

        Optional<UserModel> user = emails.stream().map(email -> context.getSession().users().getUserByEmail(email, context.getRealm())).findFirst();
        if (user.isPresent()) {
            context.setUser(user.get());
            context.success();
        } else {
            context.failure(AuthenticationFlowError.UNKNOWN_USER);
        }
    }

    @Override
    public void action(AuthenticationFlowContext context) { }

    @Override
    public void close() { }
}