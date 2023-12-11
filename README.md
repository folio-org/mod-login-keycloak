# mod-login-keycloak

Copyright (C) 2023-2023 The Open Library Foundation

This software is distributed under the terms of the Apache License,
Version 2.0. See the file "[LICENSE](LICENSE)" for more information.

## Table of contents

* [Introduction](#introduction)
* [Environment Variables](#environment-variables)
* [Loading of client IDs/secrets](#loading-of-client-idssecrets)
* [Interaction with keycloak](#interaction-with-keycloak)

## Introduction
mod-login-keycloak provides following functionality:

* Uses keycloak token to authenticate in the okapi gateway.
* Application health and availability monitoring.

## Environment Variables

| Name                         | Default value         | Required | Description                                                 |
|:-----------------------------|:----------------------|:--------:|:------------------------------------------------------------|
| KC_URL                       | -                     |   true   | Keycloak URL                                                |
| KC_LOGIN_CLIENT_SUFFIX       | login-application     |  false   | Keycloak client suffix                                      |
| DB_HOST                      | localhost             |  false   | Postgres hostname                                           |
| DB_PORT                      | 5432                  |  false   | Postgres port                                               |
| DB_USERNAME                  | postgres              |  false   | Postgres username                                           |
| DB_PASSWORD                  | postgres              |  false   | Postgres username password                                  |
| DB_DATABASE                  | postgres              |  false   | Postgres database name                                      |
| KC_CONFIG_STORE_TYPE         | ephemeral             |  false   | Secure storage type                                         |
| ENV                          | folio                 |  false   | Environment name                                            |
| KC_CONFIG_TTL                | 3600s                 |  false   | Client credentials expiration timeout                       |
| KC_ADMIN_TOKEN_TTL           | 410s                  |  false   | Admin token cache ttl                                       |
| KC_ADMIN_PASSWORD            | keycloak_system_admin |   true   | Keycloak admin password                                     |
| X_OKAPI_TOKEN_HEADER_ENABLED | false                 |  false   | Enable `x-okapi-token` header for login similar API methods |
| LOGIN_COOKIE_SAMESITE        | None                  |  false   | Value for the `SameSite` attribute in a cookie header       |
| login.cookie.samesite        | None                  |  false   | Alias for `LOGIN_COOKIE_SAMESITE`                           |

### Secure storage environment variables

#### AWS-SSM

Required when `SECRET_STORE_TYPE=AWS_SSM`

| Name                                          | Default value | Description                                                                                                                                                    |
|:----------------------------------------------|:--------------|:---------------------------------------------------------------------------------------------------------------------------------------------------------------|
| SECRET_STORE_AWS_SSM_REGION                   | -             | The AWS region to pass to the AWS SSM Client Builder. If not set, the AWS Default Region Provider Chain is used to determine which region to use.              |
| SECRET_STORE_AWS_SSM_USE_IAM                  | true          | If true, will rely on the current IAM role for authorization instead of explicitly providing AWS credentials (access_key/secret_key)                           |
| SECRET_STORE_AWS_SSM_ECS_CREDENTIALS_ENDPOINT | -             | The HTTP endpoint to use for retrieving AWS credentials. This is ignored if useIAM is true                                                                     |
| SECRET_STORE_AWS_SSM_ECS_CREDENTIALS_PATH     | -             | The path component of the credentials endpoint URI. This value is appended to the credentials endpoint to form the URI from which credentials can be obtained. |

#### Vault

Required when `SECRET_STORE_TYPE=VAULT`

| Name                                    | Default value | Description                                                                         |
|:----------------------------------------|:--------------|:------------------------------------------------------------------------------------|
| SECRET_STORE_VAULT_TOKEN                | -             | token for accessing vault, may be a root token                                      |
| SECRET_STORE_VAULT_ADDRESS              | -             | the address of your vault                                                           |
| SECRET_STORE_VAULT_ENABLE_SSL           | false         | whether or not to use SSL                                                           |
| SECRET_STORE_VAULT_PEM_FILE_PATH        | -             | the path to an X.509 certificate in unencrypted PEM format, using UTF-8 encoding    |
| SECRET_STORE_VAULT_KEYSTORE_PASSWORD    | -             | the password used to access the JKS keystore (optional)                             |
| SECRET_STORE_VAULT_KEYSTORE_FILE_PATH   | -             | the path to a JKS keystore file containing a client cert and private key            |
| SECRET_STORE_VAULT_TRUSTSTORE_FILE_PATH | -             | the path to a JKS truststore file containing Vault server certs that can be trusted |

### Keycloak environment variables

Keycloak all configuration properties: https://www.keycloak.org/server/all-config

| Name               | Description                                                                                                                                                                |
|:-------------------|:---------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| KC_HOSTNAME        | Keycloak hostname, will be added to returned endpoints, for example for openid-configuration                                                                               |
| KC_ADMIN           | Initial admin username                                                                                                                                                     |
| KC_ADMIN_PASSWORD  | Initial admin password                                                                                                                                                     |
| KC_DB              | Database type                                                                                                                                                              |
| KC_DB_URL_DATABASE | Sets the database name of the default JDBC URL of the chosen vendor. If the DB_URL option is set, this option is ignored.                                                  |
| KC_DB_URL_HOST     | Sets the hostname of the default JDBC URL of the chosen vendor. If the DB_URL option is set, this option is ignored.                                                       |
| KC_DB_URL_PORT     | Sets the port of the default JDBC URL of the chosen vendor. If the DB_URL option is set, this option is ignored.                                                           |
| KC_DB_USERNAME     | Database Username                                                                                                                                                          |
| KC_DB_PASSWORD     | Database Password                                                                                                                                                          |
| KC_PROXY           | The proxy address forwarding mode if the server is behind a reverse proxy. Possible values are: edge, reencrypt, passthrough. https://www.keycloak.org/server/reverseproxy |
| KC_HOSTNAME_STRICT | Disables dynamically resolving the hostname from request headers. Should always be set to true in production, unless proxy verifies the Host header.                       |
| KC_HOSTNAME_PORT   | The port used by the proxy when exposing the hostname. Set this option if the proxy uses a port other than the default HTTP and HTTPS ports. Defaults to -1.               |

## Loading of client IDs/secrets

The module pulls client_secret for client_id from AWS Parameter store, Vault or other reliable secret storages when they
are required for login. The credentials are cached for 3600s.

## Interaction with keycloak

### Authenticate with Keycloak (using user's credentials) and get back an access token and refresh token
```shell
curl -XPOST \
-H "Content-Type: application/x-www-form-urlencoded" \
--data-urlencode "username=$username" \
--data-urlencode "password=$password" \
--data-urlencode "client_id=$clientId" \
--data-urlencode "client_secret=$clientSecret" \
--data-urlencode "grant_type=password" \
--data-urlencode "scopes=openid profile email" \
"$keycloakUrl/realms/$tenantId/protocol/openid-connect/token"
```

### Get User Info
```shell
curl -XGET \
-H "Content-Type: application/x-www-form-urlencoded" \
-H "Authorization: Bearer $acessToken" \
"$keycloakUrl/realms/$tenantId/protocol/openid-connect/userinfo"
```

### Introspect Token
```shell
curl -XPOST \
-H "Content-Type: application/x-www-form-urlencoded" \
--data-urlencode "token=$accessToken" \
--data-urlencode "client_id=$clientId" \
--data-urlencode "client_secret=$clientSecret" \
"$keycloakUrl/realms/$tenantId/protocol/openid-connect/token/introspect"
```

### Refresh token
```shell
curl -XPOST \
-H "Content-Type: application/x-www-form-urlencoded" \
--data-urlencode "refresh_token=$refreshToken" \
--data-urlencode "client_id=$clientId" \
--data-urlencode "client_secret=$clientSecret" \
--data-urlencode "grant_type=refresh_token" \
"$keycloakUrl/realms/$tenantId/protocol/openid-connect/token"
```

### Logout
```shell
curl -XPOST \
-H "Content-Type: application/x-www-form-urlencoded" \
--data-urlencode "refreshToken=$refreshToken" \
--data-urlencode "client_id=$clientId" \
--data-urlencode "client_secret=$clientSecret" \
"$keycloakUrl/realms/$tenantId/protocol/openid-connect/logout"
```

### Reset password
```shell
curl -XPUT \
-H "Content-Type: application/json" \
-H "Authorization: $KEYCLOAK_TOKEN" \
--data-raw '{
    "temporary": false,
    "type": "password",
    "value": "demouser"
}'
"$keycloakUrl/admin/realms/$tenantId/users/{userId}/reset-password"
```

### Get Credentials
```shell
curl -XGET \
-H "Content-Type: application/json" \
-H "Authorization: $KEYCLOAK_TOKEN" \
"$keycloakUrl/admin/realms/$tenantId/users/{userId}/credentials"
```

### Delete Credentials
```shell
curl -XDELETE \
-H "Content-Type: application/json" \
-H "Authorization: $KEYCLOAK_TOKEN" \
"$keycloakUrl/admin/realms/$tenantId/users/{userId}/credentials/{credentialId}"
```
