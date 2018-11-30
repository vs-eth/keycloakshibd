# keycloakshibd

Offers E-Mail based Shibboleth authentication for Keycloak.

Users are authenticated via SwitchAAI and then redirected to Keycloak. If configured, this plugin
will extract the users' E-Mail and look him up the user database.

The flow proceeds as follows:
1. An (unauthenticated) user has to authenticate on the `/protocol/openid-connect/auth` endpoint
2. The user is redirected to wayf.switch.ch and chooses his home university
3. The user signs in to his universities' account and is redirected to Keycloak

If a user matching the E-Mail
address returned by SwitchAAI is found, he will be authenticated to Keycloak.

## Prerequisites

* OpenID Connect / OAuth 2.0 based authentication method for Keycloak
* Shibboleth-configured Apache-Server protecting the `/protocol/openid-connect/auth` path
    * The following fields MUST be included in the SwitchAAI resource description:
        * email
        * persistendId / targetedId (for unique identification of external users)
* (optional) User Federation with unique E-Mail Addresses
    * Useful if the authenticated users should automatically be federated

## Installation

* Build the project using `mvn build`
* Put the generated jar in the `/standalone/deployments/` (resp. `/standalone-ha/deployments/`)
  folder of your keycloak installation
* Configure the `Shibboleth` execution flow as Authentication requirement for type `Browser`
* Restart Keycloak (don't know if this is necessary)
