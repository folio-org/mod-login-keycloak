# mod-login-keycloak

FOLIO authentication module using Keycloak as identity provider — handles authentication, token management, credential operations, and password reset. PostgreSQL for password-reset persistence. Java, Spring Boot, PostgreSQL/Liquibase, Kafka, Keycloak.

## Build & Test

```bash
mvn clean package              # build JAR
mvn clean verify               # build + all tests
mvn test                       # unit tests (@UnitTest, surefire)
mvn test -Dtest='*Credential*' # unit tests matching pattern
mvn verify -Dit.test=ClassName -Dtest=SkipSurefire -Dsurefire.failIfNoSpecifiedTests=false # single integration test (@IntegrationTest, failsafe; needs Docker)
mvn checkstyle:check
```

Integration tests use Testcontainers (Keycloak, Postgres, Kafka, WireMock).

## Conventions

- **Exceptions**: `NotFoundException` (folio-spring, missing Keycloak resource), `ServiceException` (infra/HTTP client), `RequestValidationException` (business), `EntityNotFoundException` (JPA).
- **Config**: OpenAPI spec `src/main/resources/swagger.api/mod-login-keycloak.yaml`; env vars in `README.md`; secure store key prefix is `SECURE_STORE_ENV`, falling back to `ENV`, then `folio`. Realm config via `RealmConfigurationProvider`; client secrets cached (`KC_CONFIG_TTL`, default 3600s).
- **PR process**: update `NEWS.md` with JIRA key; PR template = Purpose / Approach (Summary 2-3 sentences + bullet Implementation Details) / Pre-Review Checklist; tests + checkstyle must pass.

## Testing

- **Unit** (`@UnitTest`, in `service/`, `controller/`): Mockito `@ExtendWith(MockitoExtension.class)`; never lenient; stub only what's used; verify only unmocked; `verifyNoMoreInteractions()` in `@AfterEach`; name `methodName_scenario_expectedBehavior`.
- **Integration** (`@IntegrationTest`, in `it/`): extend `BaseIntegrationTest`; `@KeycloakRealms` loads realm from `src/test/resources/json/keycloak/test-realm.json`; `@WireMockStub` (stubs in `src/test/resources/wiremock/stubs/`); Testcontainers auto-start.
- **Test data**: `TestConstants.java` (USER_ID, ADMIN_USER_ID, TENANT), `TestValues.java` factories. In test realm, `USER_ID 99999999-...` has password credentials; `ADMIN_USER_ID 11111111-...` does not.
