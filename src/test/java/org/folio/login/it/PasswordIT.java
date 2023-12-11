package org.folio.login.it;

import static org.folio.login.support.TestConstants.PASSWORD_RESET_ACTION_ID;
import static org.folio.login.support.TestConstants.TENANT;
import static org.folio.login.support.TestConstants.USER_ID;
import static org.folio.login.support.TestValues.passwordCreateAction;
import static org.folio.login.support.TestValues.passwordResetAction;
import static org.folio.test.TestUtils.asJsonString;
import static org.folio.test.TestUtils.parseResponse;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.SqlMergeMode.MergeMode.MERGE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import javax.sql.DataSource;
import org.folio.login.domain.dto.Password;
import org.folio.login.domain.dto.ResponseCreateAction;
import org.folio.login.domain.dto.ResponseResetAction;
import org.folio.login.support.base.BaseIntegrationTest;
import org.folio.spring.integration.XOkapiHeaders;
import org.folio.test.extensions.KeycloakRealms;
import org.folio.test.types.IntegrationTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;

@IntegrationTest
@SqlMergeMode(MERGE)
@Sql(scripts = "classpath:/sql/truncate-auth-password-action-table.sql", executionPhase = AFTER_TEST_METHOD)
class PasswordIT extends BaseIntegrationTest {

  @BeforeAll
  static void beforeAll(@Autowired DataSource datasource) {
    var resourceDatabasePopulator = new ResourceDatabasePopulator();
    resourceDatabasePopulator.addScript(new ClassPathResource("/sql/create-schema-for-auth-password-actions.sql"));
    resourceDatabasePopulator.execute(datasource);
  }

  @Test
  @KeycloakRealms(realms = "/json/keycloak/test-realm.json")
  void createResetPasswordAction_positive() throws Exception {
    var response = mockMvc.perform(post("/authn/password-reset-action")
        .contentType(APPLICATION_JSON)
        .header(XOkapiHeaders.TENANT, TENANT)
        .content(asJsonString(passwordCreateAction())))
      .andExpect(status().isCreated())
      .andReturn();
    var createAction = parseResponse(response, ResponseCreateAction.class);
    assertNotNull(createAction);
  }

  @Test
  @KeycloakRealms(realms = "/json/keycloak/test-realm.json")
  void createResetPasswordAction_negative() throws Exception {
    var userId = UUID.randomUUID().toString();
    mockMvc.perform(post("/authn/password-reset-action")
        .contentType(APPLICATION_JSON)
        .header(XOkapiHeaders.TENANT, TENANT)
        .content(asJsonString(passwordCreateAction(userId))))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.total_records", is(1)))
      .andExpect(jsonPath("$.errors[0].message",
        is("Keycloak user doesn't exist with the given 'user_id' attribute: " + userId)))
      .andExpect(jsonPath("$.errors[0].type", is("NotFoundException")))
      .andExpect(jsonPath("$.errors[0].code", is("not_found_error")));
  }

  @Test
  @Sql("classpath:/sql/populate-auth-password-action.sql")
  @KeycloakRealms(realms = "/json/keycloak/test-realm.json")
  void getResetPasswordActionById_positive() throws Exception {
    var response = mockMvc.perform(get("/authn/password-reset-action/{actionId}", PASSWORD_RESET_ACTION_ID)
        .contentType(APPLICATION_JSON)
        .header(XOkapiHeaders.TENANT, TENANT))
      .andExpect(status().isOk())
      .andReturn();
    var passwordAction = parseResponse(response, ResponseCreateAction.class);
    assertNotNull(passwordAction);
  }

  @Test
  @KeycloakRealms(realms = "/json/keycloak/test-realm.json")
  void getResetPasswordActionById_negative() throws Exception {
    mockMvc.perform(get("/authn/password-reset-action/{actionId}", PASSWORD_RESET_ACTION_ID)
        .contentType(APPLICATION_JSON)
        .header(XOkapiHeaders.TENANT, TENANT))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.total_records", is(1)))
      .andExpect(jsonPath("$.errors[0].message",
        is("Password action with ID: " + PASSWORD_RESET_ACTION_ID + " was not found in the db")))
      .andExpect(jsonPath("$.errors[0].type", is("EntityNotFoundException")))
      .andExpect(jsonPath("$.errors[0].code", is("not_found_error")));
  }

  @Test
  @Sql("classpath:/sql/populate-auth-password-action.sql")
  @KeycloakRealms(realms = "/json/keycloak/test-realm.json")
  void resetAction_positive() throws Exception {
    var response = mockMvc.perform(post("/authn/reset-password")
        .contentType(APPLICATION_JSON)
        .content(asJsonString(passwordResetAction()))
        .header(XOkapiHeaders.TENANT, TENANT))
      .andExpect(status().isCreated())
      .andReturn();
    var resetAction = parseResponse(response, ResponseResetAction.class);
    assertNotNull(resetAction);
  }

  @Test
  void validatePasswordRepeatability_positive() throws Exception {
    var password = new Password().password("password").userId(USER_ID);

    mockMvc.perform(post("/authn/password/repeatable")
        .contentType(APPLICATION_JSON)
        .content(asJsonString(password))
        .header(XOkapiHeaders.TENANT, TENANT))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.result", is("valid")));
  }

  @Test
  void validatePasswordRepeatability_negative_badRequest() throws Exception {
    var password = new Password().password("password");

    mockMvc.perform(post("/authn/password/repeatable")
        .contentType(APPLICATION_JSON)
        .content(asJsonString(password))
        .header(XOkapiHeaders.TENANT, TENANT))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.total_records", is(1)))
      .andExpect(jsonPath("$.errors[0].message", is("must not be null")))
      .andExpect(jsonPath("$.errors[0].type", is("MethodArgumentNotValidException")))
      .andExpect(jsonPath("$.errors[0].code", is("validation_error")))
      .andExpect(jsonPath("$.errors[0].parameters[0].key", is("userId")));
  }
}
