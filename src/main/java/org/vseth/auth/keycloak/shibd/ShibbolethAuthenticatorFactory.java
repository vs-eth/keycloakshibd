package org.vseth.auth.keycloak.shibd;

import org.keycloak.Config;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.ConfigurableAuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.ArrayList;
import java.util.List;

public class ShibbolethAuthenticatorFactory implements AuthenticatorFactory, ConfigurableAuthenticatorFactory {

    public static final String PROVIDER_ID = "shibboleth-authenticator";
    private static final ShibbolethAuthenticator SINGLETON = new ShibbolethAuthenticator();

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public Authenticator create(KeycloakSession session) {
        return SINGLETON;
    }

    private static final AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
            AuthenticationExecutionModel.Requirement.DISABLED,
            AuthenticationExecutionModel.Requirement.OPTIONAL,
            AuthenticationExecutionModel.Requirement.REQUIRED,
    };
    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }

    @Override
    public boolean isConfigurable() {
        return true;
    }

    @Override
    public boolean isUserSetupAllowed() {
        return false;
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }

    private static final List<ProviderConfigProperty> configProperties = new ArrayList<>();
    static {
        ProviderConfigProperty property = new ProviderConfigProperty();
        property.setName("email-header");
        property.setLabel("E-Mail header");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setHelpText("The HTTP Header name of the E-Mail address");
        configProperties.add(property);
    }

    @Override
    public String getDisplayType() {
        return "Shibboleth";
    }

    @Override
    public String getReferenceCategory() {
        return "Shibboleth";
    }

    @Override
    public String getHelpText() {
        return "Shibboleth Authentication via HTTP headers passed by Apache";
    }

    @Override
    public void init(Config.Scope scope) { }

    @Override
    public void postInit(KeycloakSessionFactory sessionFactory) { }

    @Override
    public void close() { }
}
