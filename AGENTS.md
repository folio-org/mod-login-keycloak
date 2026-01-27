# AGENTS.md - Coding Agent Guidelines for mod-login-keycloak

## Project Overview

mod-login-keycloak is a FOLIO module that provides authentication functionality using Keycloak as the identity provider. It handles user authentication, token management, credential operations, and password reset workflows.

## Build and Test Commands

### Building
```bash
mvn clean compile              # Compile source code
mvn clean package              # Build JAR file
mvn clean verify               # Build + run all tests
```

### Testing
```bash
# Unit tests (tagged with @UnitTest, uses maven-surefire-plugin)
mvn test                                    # Run all unit tests
mvn test -Dtest=ClassName                   # Run specific test class
mvn test -Dtest='*Credential*'              # Run tests matching pattern

# Integration tests (tagged with @IntegrationTest, uses maven-failsafe-plugin)
mvn verify                                  # Run unit + integration tests
mvn verify -Dit.test=ClassName              # Run specific integration test
mvn failsafe:integration-test               # Run only integration tests

# Note: Integration tests require Docker (Testcontainers for Keycloak, Postgres, Kafka, WireMock)
```

### Code Quality
```bash
mvn checkstyle:check           # Run checkstyle validation
```

## Architecture

### Service Layer Hierarchy

The service layer has three distinct tiers with clear responsibilities:

1. **KeycloakService** (Low-level Keycloak operations)
   - Direct integration with Keycloak via `KeycloakClient` (Feign client)
   - Handles token operations, credential CRUD, user management
   - Propagates Keycloak exceptions (FeignException, NotFoundException) to callers
   - Uses `AdminTokenService` for admin token management (with caching)

2. **CredentialsService / PasswordService** (Business logic layer)
   - **CredentialsService**: Wraps KeycloakService for credential operations, catches NotFoundException and converts to business-friendly responses (e.g., credentialsExist=false)
   - **PasswordService**: Manages password reset workflow with database persistence, requires users to exist (lets NotFoundException propagate)
   - Both services coordinate between Keycloak operations and other integrations (UsersKeycloakClient, database)

3. **LoginService** (High-level authentication flows)
   - Orchestrates complete authentication flows (login, logout, token refresh)
   - Coordinates between KeycloakService, LoginAttemptsService, and LogEventsService
   - Publishes Kafka events for logout operations

**Key Pattern**: Each layer has specific exception handling strategies:
- KeycloakService: Wraps FeignException in ServiceException for generic Keycloak errors, propagates NotFoundException for missing users
- CredentialsService: Catches NotFoundException, returns credentialsExist=false (user-friendly)
- PasswordService: Lets NotFoundException propagate (user must exist for password operations)

### Integration Points

- **KeycloakClient** (Feign): Direct HTTP client to Keycloak REST API
- **UsersKeycloakClient**: Integration with FOLIO users module via mod-users-keycloak
- **Secure Store**: Retrieves client secrets from AWS SSM, Vault, or FSSP (configured via KC_CONFIG_STORE_TYPE)
- **Kafka**: Publishes logout events (supports both per-tenant and consolidated topics based on KAFKA_PRODUCER_TENANT_COLLECTION)
- **Database**: Postgres for password reset action persistence

### Testing Strategy

**Unit Tests** (`@UnitTest`, located in `src/test/java/.../service`, `src/test/java/.../controller`):
- Use Mockito with `@ExtendWith(MockitoExtension.class)`
- Follow strict mocking guidelines (see `doc/ai/UnitTesting.md`):
  - Never use lenient mode
  - Only stub what's used in the test
  - Only verify unmocked interactions
  - Use `verifyNoMoreInteractions()` in `@AfterEach`
  - Test naming: `methodName_scenario_expectedBehavior`

**Integration Tests** (`@IntegrationTest`, located in `src/test/java/.../it`):
- Extend `BaseIntegrationTest`
- Use `@KeycloakRealms` to load test realm data (from `src/test/resources/json/keycloak/`)
- Use `@WireMockStub` for external service mocking
- Testcontainers automatically spin up Keycloak, Postgres, Kafka, WireMock

**Test Data**:
- Constants in `TestConstants.java`: USER_ID, ADMIN_USER_ID, TENANT, etc.
- Factory methods in `TestValues.java`: `loginCredentials()`, `passwordResetAction()`, etc.
- Keycloak test realm: `src/test/resources/json/keycloak/test-realm.json`
  - USER_ID (99999999-9999-4999-9999-999999999999): Has password credentials
  - ADMIN_USER_ID (11111111-2222-4999-0000-999999999999): No password credentials

## Code Conventions

### Exception Handling
- **NotFoundException** (from folio-spring): User/resource not found in Keycloak
- **ServiceException**: Wraps Keycloak/infrastructure errors (FeignException)
- **RequestValidationException**: Business validation failures (e.g., duplicate credentials)
- **EntityNotFoundException** (JPA): Database entity not found

### Pull Request Process
1. Update `NEWS.md` with JIRA issue key and description
2. Follow PR template structure: Purpose, Approach, Pre-Review Checklist
3. Approach section format:
   - Summary: 2-3 sentences
   - Implementation Details: Bullet list, each point max 2 sentences
4. Ensure all tests pass (unit + integration)
5. Checkstyle must pass

### Configuration Files
- `src/main/resources/swagger.api/mod-login-keycloak.yaml`: OpenAPI specification
- Environment variables documented in README.md (Keycloak, database, secure store, Kafka)
- Secure store configuration: Use SECURE_STORE_ENV (not ENV) for key prefix

## Common Workflows

### Adding a New Service Method
1. Add method to appropriate service (KeycloakService for low-level, CredentialsService/PasswordService for business logic)
2. Write unit tests following `doc/ai/UnitTesting.md` guidelines
3. If exposing via REST API, add integration test in corresponding IT class
4. Update NEWS.md with change description

### Working with Keycloak Integration
- Admin operations require admin token from `AdminTokenService.getAdminToken()`
- User-facing operations use user's credentials or tokens
- Realm configuration loaded via `RealmConfigurationProvider`
- Client secrets retrieved from secure store (cached with KC_CONFIG_TTL, default 3600s)

### Debugging Integration Tests
- Check WireMock stubs in `src/test/resources/wiremock/stubs/`
- Testcontainer logs available via `Slf4jLogConsumer` in test output
- Test Keycloak realm at: `src/test/resources/json/keycloak/test-realm.json`
