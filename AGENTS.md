# mod-login-keycloak

FOLIO authentication module using Keycloak as identity provider — handles authentication, token management, credential operations, and password reset. PostgreSQL for password-reset persistence.

## Build & Test

```bash
mvn clean package              # build JAR
mvn clean verify               # build + all tests
mvn test                       # unit tests (@UnitTest, surefire)
mvn test -Dtest='*Credential*' # unit tests matching pattern
mvn verify -Dit.test=ClassName # single integration test (@IntegrationTest, failsafe; needs Docker)
mvn checkstyle:check
```

Integration tests use Testcontainers (Keycloak, Postgres, Kafka, WireMock).

## Architecture

**Three-tier service layer**:
1. **KeycloakService** — low-level Keycloak ops via `KeycloakClient` (Feign): tokens, credential CRUD, users. Uses `AdminTokenService` (cached). Wraps `FeignException` in `ServiceException`; propagates `NotFoundException`.
2. **CredentialsService / PasswordService** — business logic. CredentialsService catches `NotFoundException` → `credentialsExist=false`; PasswordService lets it propagate (user must exist). Both coordinate Keycloak + `UsersKeycloakClient` + DB.
3. **LoginService** — high-level flows (login, logout, token refresh); coordinates LoginAttemptsService + LogEventsService; publishes Kafka logout events.

**Integrations**: `KeycloakClient` (Feign → Keycloak REST), `UsersKeycloakClient` (mod-users-keycloak), secure store (AWS-SSM/Vault/FSSP via `KC_CONFIG_STORE_TYPE`), Kafka logout events (per-tenant or consolidated via `KAFKA_PRODUCER_TENANT_COLLECTION`), Postgres.

## Conventions

- **Exceptions**: `NotFoundException` (folio-spring, missing Keycloak resource), `ServiceException` (infra/Feign), `RequestValidationException` (business), `EntityNotFoundException` (JPA).
- **Config**: OpenAPI spec `src/main/resources/swagger.api/mod-login-keycloak.yaml`; env vars in `README.md`; secure store uses `SECURE_STORE_ENV` (not `ENV`) for key prefix. Realm config via `RealmConfigurationProvider`; client secrets cached (`KC_CONFIG_TTL`, default 3600s).
- **PR process**: update `NEWS.md` with JIRA key; PR template = Purpose / Approach (Summary 2-3 sentences + bullet Implementation Details) / Pre-Review Checklist; tests + checkstyle must pass.

## Testing

- **Unit** (`@UnitTest`, in `service/`, `controller/`): Mockito `@ExtendWith(MockitoExtension.class)`; never lenient; stub only what's used; verify only unmocked; `verifyNoMoreInteractions()` in `@AfterEach`; name `methodName_scenario_expectedBehavior`. Guide: `doc/ai/UnitTesting.md`.
- **Integration** (`@IntegrationTest`, in `it/`): extend `BaseIntegrationTest`; `@KeycloakRealms` loads realm from `src/test/resources/json/keycloak/test-realm.json`; `@WireMockStub` (stubs in `src/test/resources/wiremock/stubs/`); Testcontainers auto-start.
- **Test data**: `TestConstants.java` (USER_ID, ADMIN_USER_ID, TENANT), `TestValues.java` factories. In test realm, `USER_ID 99999999-...` has password credentials; `ADMIN_USER_ID 11111111-...` does not.
