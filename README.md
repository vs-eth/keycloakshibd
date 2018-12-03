# keycloakshibd

Offers E-Mail based Shibboleth authentication for Keycloak.

Users are authenticated via SwitchAAI and then redirected to Keycloak. If configured, this plugin
will extract the users' E-Mail and look him up the user database.

The flow proceeds as follows:
1. An (unauthenticated) user has to authenticate on the `/protocol/openid-connect/auth` endpoint
2. The user is redirected to wayf.switch.ch and chooses his home university
3. The user signs in to his universities' account and is redirected to Keycloak

If a user matching the E-Mail address returned by SwitchAAI is found, he will be authenticated
to Keycloak. If not, a new user will be created with that E-Mail address, with the following
username:

    ext-user-<address>-<domain>
    
Where `<address>` and `<domain>` come from the user's E-Mail `<address>@<domain>`.

## Prerequisites

* OpenID Connect / OAuth 2.0 based authentication method for Keycloak
* Shibboleth-configured Apache-Server protecting the `/protocol/openid-connect/auth` path
    * The following fields MUST be included in the SwitchAAI resource description:
        * givenName
        * surname
        * email
        * persistendId / targetedId (for unique identification of external users)
* (optional) User Federation via unique E-Mail Addresses
    * Useful if the authenticated users should automatically be federated
    
## Installation

* Build the project using `mvn build`
* Put the generated jar in the `/standalone/deployments/` (resp. `/standalone-ha/deployments/`)
  folder of your keycloak installation
* Configure the `Shibboleth` execution flow as Authentication requirement for type `Browser`
    * You can choose between `Optional` and `Required`
* Restart Keycloak (don't know if this is necessary)

## Pitfalls

This plugin speeds up user lookups via E-Mail addresses - that is, ETH or external users are
looked up by their E-Mail address first, and if none is found, matched on the known persistent
IDs, which can lead to shadowing (A person's) ETH AAI account and their eduID-Account can have
the same E-Mail address. In this case the plugin prioritizes the ETH account, which is expected
and intended behaviour.
