# mod-login-keycloak

Login, token refresh, logout, and credential and password management for FOLIO users, backed by one Keycloak realm per tenant. PostgreSQL stores only password-reset actions.

## Commands

```bash
mvn clean package -DskipTests  # build
mvn test                       # unit tests (@UnitTest, surefire group "unit")
mvn verify                     # unit and integration tests (@IntegrationTest, *IT.java); needs Docker
mvn test -Dtest=KeycloakServiceTest#getUserToken_positive  # one unit test
mvn verify -Dit.test=LoginIT#login_positive -Dtest=SkipSurefire -Dsurefire.failIfNoSpecifiedTests=false  # one IT, unit tests skipped
mvn checkstyle:check           # style only
```

Integration tests extend `BaseIntegrationTest`, which starts Keycloak (TLS mode), PostgreSQL, Kafka, and WireMock with Testcontainers.

Checkstyle also runs at `process-classes`, so every build above fails on a violation, test sources included. The base rules come from `folio-java-checkstyle`; this repository limits methods to 21 lines (`checkstyle/checkstyle-checker.properties`), and test sources are exempt from that limit.

## Conventions

- The OpenAPI spec `src/main/resources/swagger.api/mod-login-keycloak.yaml` generates `org.folio.login.rest.resource` and `org.folio.login.domain.dto`. Change the spec, not the generated sources.
- Keycloak is called through `KeycloakClient` and `KeycloakUserClient` (built in `KeycloakClientConfiguration` against `KC_URL` with the Keycloak TLS settings) and through the Keycloak admin SDK bean from `KeycloakAdminConfiguration` (used by `LoginAttemptsService` and `LogEventsService`). Module clients (`UsersClient`, `UsersKeycloakClient`) come from the shared `HttpServiceProxyFactory` in `ClientConfig`. Register a new client in the matching place, because base URL and TLS setup differ.
- The logout topic `mod-login-keycloak.logout` is declared in `producesEvents` of `descriptors/ModuleDescriptor-template.json` and in `application.kafka.tenant-topics`, from which `ModuleCustomTenantService` creates topics on tenant enable. A new topic needs both.
- A new environment variable goes into `application.yml`, the README table, and `DeployConfig.json` (the deployment template).
- Add a `NEWS.md` entry with the Jira key. PR descriptions follow `.github/PULL_REQUEST_TEMPLATE.md`.

## Gotchas

- Failed `KeycloakClient` calls and secure-store failures return 400, not 5xx: `ServiceException` maps to 400 `service_error`, and `IllegalStateException` (secure-store failure, more than one Keycloak user with the same `user_id` attribute) to 400 as well.
- Keycloak answers bad login credentials with 400 `invalid_grant`. `KeycloakService.getToken` turns that into 401 only for the password grant; for the authorization-code grant it stays 400 `service_error`.
- `LoginService` wraps every refresh and logout exception into `TokenRefreshException` or `TokenLogoutException`, both 422. An expired refresh token gives 422, not 401, and an unparsable refresh token on logout gives 422, not the 400 of `TokenParsingException`.
- In `LoginController` the cookie parameter names are swapped: `logout(folioRefreshTokenRequired)` is optional and `refreshToken(folioRefreshToken)` is required. The spec decides (its `folioRefreshToken` and `folioRefreshTokenRequired` parameters). A logout without the refresh cookie logs out all sessions of the user.
- Refresh takes the tenant from the refresh token issuer, not from `x-okapi-tenant`: `KeycloakService.refreshToken` overrides the header in a new execution context (`refresh_positive_crossTenantRequest_tenantHeaderIsDifferentFromTokenRealm`).
- Logout and logout-all publish the Kafka event before calling Keycloak, so the event is sent even when the Keycloak call then fails. The future from `kafkaTemplate.send` is not awaited, so an asynchronous send failure does not fail the request.
- Logout and logout-all always expire every cookie the client sent, not only `folioAccessToken` and `folioRefreshToken`. A failed `GET /authn/token` does the same; a failed refresh does not. The failure paths are listed twice, in `FilterConfig` (servlet filter) and in `@InvalidateCookiesOnException` on `ApiExceptionHandler`; keep both in sync.
- `RealmConfigurationProvider` caches the tenant's login client ID and secret for `KC_CONFIG_TTL` (default 3600s). The secret is read from the secure store at key `<env>_<tenant>_<tenant><KC_LOGIN_CLIENT_SUFFIX>`, where `<env>` is `SECURE_STORE_ENV`, then `ENV`, then `folio`. Nothing evicts the entry on a Keycloak 401, so a rotated secret is used only after the TTL or a restart.
- `PasswordService` is `@Transactional`, but Keycloak changes are not rolled back. `resetAction` changes the password before the action row is deleted, and the `DELETE` executes at commit.
- `CredentialsService.checkCredentialsExistence` treats a missing Keycloak user as `credentialsExist=false`, while `PasswordService` lets `NotFoundException` propagate as 404 (the user must exist). Keep this difference when touching either flow.
- In the test realm (`src/test/resources/json/keycloak/test-realm.json`), `USER_ID` from `TestConstants` has a password credential and `ADMIN_USER_ID` has none. Use the admin user for tests that create credentials.
