# keycloakshibd

Offers E-Mail based Shibboleth authentication for Keycloak.

Users are authenticated via SwitchAAI and then redirected to Keycloak. If configured, this plugin
will extract the users' E-Mail and look him up the user database.

The flow proceeds as follows:
1. An (unauthenticated) user has to authenticate on the `/protocol/openid-connect/auth` endpoint
2. The user is redirected to the configured Shibboleth login page and chooses his home organization
    * In our case, this is `wayf.switch.ch`
3. The user signs in to his universities' account and is redirected to Keycloak

If a user matching the E-Mail address returned by the Shibboleth IdP is found, he will be
authenticated to Keycloak. If not, the plugin will check the persistent id and whether
a user with that particular id can be found (this can happen e.g. when a user changes his
E-Mail address but keeps his Shibboleth profile.). If no user matching the persistent ID is
found, a new user will be created with the provided E-Mail address and the following username:

    ext-user-<address>-<domain>
    
Where `<address>` and `<domain>` are taken from the user's E-Mail `<address>@<domain>`.

## Prerequisites

* OpenID Connect / OAuth 2.0 based authentication method for Keycloak
* Shibboleth-configured Apache-Server protecting the `/protocol/openid-connect/auth` path
    * The following fields MUST be included in the SwitchAAI resource description:
        * givenName
        * surname
        * email
        * persistendId / targetedId (for unique identification of external users)
* (optional) User Federation via unique E-Mail Addresses
    * Useful if the authenticated users should automatically be federated (e.g. from
     the universities' LDAP)
    
## Installation

* Build the project using `mvn build`  
  or use Maven in a docker: `docker run --rm -it -u $( id -u ) --workdir /build -v $( pwd ):/build -v $HOME/.m2:/var/maven/.m2 -e MAVEN_CONFIG=/var/maven/.m2 maven /bin/bash` und im docker dann `mvn -Dtycho.disableP2Mirrors=true -Dmaven.antrun.skip=true -Dmaven.repo.local=/var/maven/.m2 clean verify`.
* Put the generated jar in the `/standalone/deployments/` (resp. `/standalone-ha/deployments/`)
  folder of your keycloak installation
* Configure the `Shibboleth` execution flow as Authentication requirement for type `Browser`
    * You can choose between `Optional` and `Required`
* Restart Keycloak (don't know if this is necessary)

## FAQ

### Why not use the users persistent IDs as their usernames?

The persistent ID is not a human-friendly identifier for users. While it may guarantee uniqueness
of usernames, it makes interaction with poorly written software easier by providing a somewhat
sensible (and human-friendly) value in the `username` field, which can be displayed to
users.

### Why use the user's E-Mail address for lookup and not his persistent ID?

Only some fields in keycloak are indexed for fast retrieval, the most commonly used ones being
the E-Mail address and username, since Keycloak guarantees them to be unique by default. As
there currently is no way to create a new field on a user with a fast index retrieval without
messing with the internals we have decided to speed up lookups by first checking the users
E-Mail addresses before falling back to the persistent ID.

Note that this can lead to shadowing (a person's AAI accounts can map to the same user profile
in Keycloak if they use the same E-Mail address). This is known and intended behavior.
