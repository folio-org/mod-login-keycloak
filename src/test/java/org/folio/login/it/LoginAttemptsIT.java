package org.folio.login.it;

import static org.folio.login.support.TestConstants.TENANT;
import static org.folio.login.support.TestConstants.USER_ID;
import static org.folio.login.support.TestValues.invalidLoginCredentials;
import static org.folio.login.support.TestValues.loginCredentials;
import static org.folio.test.TestUtils.asJsonString;
import static org.folio.test.TestUtils.parseResponse;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import lombok.SneakyThrows;
import org.folio.login.domain.dto.LoginAttempts;
import org.folio.spring.integration.XOkapiHeaders;
import org.folio.test.base.BaseBackendIntegrationTest;
import org.folio.test.extensions.EnableKeycloakTlsMode;
import org.folio.test.extensions.EnablePostgres;
import org.folio.test.extensions.KeycloakRealms;
import org.folio.test.types.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@IntegrationTest
@EnableKeycloakTlsMode
@EnablePostgres
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@KeycloakRealms(realms = "/json/keycloak/test-realm.json")
class LoginAttemptsIT extends BaseBackendIntegrationTest {

  @Test
  void testLoginAttempts_successfulAttempt() throws Exception {
    callLoginEndpoint();

    var loginAttempts = getLoginAttempts(USER_ID);

    assertEquals(0, loginAttempts.getAttemptCount());
    assertEquals(USER_ID, loginAttempts.getUserId());
    assertNotNull(loginAttempts.getLastAttempt());
  }

  @Test
  void testLoginAttempts_failedAttempts() throws Exception {
    callLoginEndpointWithInvalidCreds();
    callLoginEndpointWithInvalidCreds();
    callLoginEndpointWithInvalidCreds();

    var loginAttempts = getLoginAttempts(USER_ID);

    assertEquals(3, loginAttempts.getAttemptCount());
    assertEquals(USER_ID, loginAttempts.getUserId());
    assertNotNull(loginAttempts.getLastAttempt());
  }

  @Test
  void testLoginAttempts_noAttempts() {
    var loginAttempts = getLoginAttempts(USER_ID);

    assertEquals(0, loginAttempts.getAttemptCount());
    assertEquals(USER_ID, loginAttempts.getUserId());
  }

  @Test
  void testLoginAttempts_unknownUser() throws Exception {
    var userId = "00e55e73-9a6f-4407-b023-e0da7a1437b8";
    mockMvc.perform(get("/authn/loginAttempts/{userId}", userId)
        .contentType(APPLICATION_JSON)
        .header(XOkapiHeaders.TENANT, TENANT))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.total_records", is(1)))
      .andExpect(jsonPath("$.errors[0].message",
        is("Keycloak user doesn't exist with the given 'user_id' attribute: " + userId)))
      .andExpect(jsonPath("$.errors[0].type", is("NotFoundException")))
      .andExpect(jsonPath("$.errors[0].code", is("not_found_error")));
  }

  private static void callLoginEndpoint() throws Exception {
    mockMvc.perform(post("/authn/login")
        .contentType(APPLICATION_JSON)
        .header(XOkapiHeaders.TENANT, TENANT)
        .content(asJsonString(loginCredentials())))
      .andExpect(status().isCreated());
  }

  private static void callLoginEndpointWithInvalidCreds() throws Exception {
    mockMvc.perform(post("/authn/login")
        .contentType(APPLICATION_JSON)
        .header(XOkapiHeaders.TENANT, TENANT)
        .content(asJsonString(invalidLoginCredentials())))
      .andExpect(status().isBadRequest());
  }

  @SneakyThrows
  private static LoginAttempts getLoginAttempts(String userId) {
    var mvcResult = mockMvc.perform(get("/authn/loginAttempts/{userId}", userId)
        .contentType(APPLICATION_JSON)
        .header(XOkapiHeaders.TENANT, TENANT))
      .andExpect(status().isOk())
      .andReturn();
    return parseResponse(mvcResult, LoginAttempts.class);
  }
}
