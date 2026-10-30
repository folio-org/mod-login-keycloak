# mod-login-keycloak

FOLIO authentication module using Keycloak as identity provider; it handles authentication, token management, credential operations, and password reset. PostgreSQL for password-reset persistence.

## Build & Test

```bash
mvn clean package              # build JAR
mvn clean verify               # build + all tests
mvn test                       # unit tests (@UnitTest, surefire)
mvn test -Dtest='*Credential*' # unit tests matching pattern
mvn verify -Dit.test=CredentialIT#createCredentials_positive -Dtest=SkipSurefire -Dsurefire.failIfNoSpecifiedTests=false  # single IT, skips unit tests (@IntegrationTest, failsafe; needs Docker)
mvn checkstyle:check
```

Integration tests use Testcontainers (Keycloak, Postgres, Kafka, WireMock).

## Style

Match the repository's existing style: use maintained implementations and tests as templates for new code, and keep the structure, naming, and conventions of neighboring files. Mockito is never lenient, stub only what a test uses, and name tests `methodName_positive` / `methodName_negative` (plus `_scenario` when needed).

## Pitfalls

- `KeycloakService` is the base service: it talks to Keycloak through `@HttpExchange` clients (no Feign in this module) and publishes the Kafka logout events. `LoginService`, `CredentialsService`, and `PasswordService` call it; `LoginAttemptsService` and `LogEventsService` are separate services called by their own controllers.
- `CredentialsService` treats a missing Keycloak user as `credentialsExist=false`, while `PasswordService` lets `NotFoundException` propagate (the user must exist). Keep this difference when touching either flow.
- Secure store: type comes from `SECRET_STORE_TYPE`, and the key prefix environment from `SECURE_STORE_ENV` (falls back to `ENV`, then `folio`).
- Kafka logout events are per-tenant or consolidated, selected by `KAFKA_PRODUCER_TENANT_COLLECTION`.
- In the test realm, the regular `USER_ID` (`TestConstants`) has password credentials and `ADMIN_USER_ID` does not. Check `TestConstants`/`TestValues` before writing credential tests.

## Testing

- Unit tests (`@UnitTest`) live in the package of the class under test; controller tests use `@WebMvcTest`.
- Integration tests (`@IntegrationTest`) extend `BaseIntegrationTest`; `@KeycloakRealms` loads the realm from `src/test/resources/json/keycloak/test-realm.json`, `@WireMockStub` loads stubs from `src/test/resources/wiremock/stubs/`.

## PR process

Update `NEWS.md` with the JIRA key. Fill in `.github/PULL_REQUEST_TEMPLATE.md` (Purpose / Approach / Pre-Review Checklist); tests and checkstyle must pass.
