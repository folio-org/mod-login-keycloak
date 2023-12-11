package org.folio.login.it;

import static org.folio.login.support.TestConstants.ADMIN_PASSWORD;
import static org.folio.login.support.TestConstants.ADMIN_USERNAME;
import static org.folio.login.support.TestConstants.ADMIN_USER_ID;
import static org.folio.login.support.TestConstants.OKAPI_URL;
import static org.folio.login.support.TestConstants.TENANT;
import static org.folio.login.support.TestConstants.USERNAME;
import static org.folio.login.support.TestConstants.USER_ID;
import static org.folio.login.support.TestValues.loginCredentials;
import static org.folio.login.support.TestValues.loginCredentialsWithoutId;
import static org.folio.login.support.TestValues.updateCredentials;
import static org.folio.test.TestUtils.asJsonString;
import static org.hamcrest.Matchers.is;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.folio.login.domain.dto.CredentialsExistence;
import org.folio.login.support.base.BaseIntegrationTest;
import org.folio.spring.integration.XOkapiHeaders;
import org.folio.test.extensions.KeycloakRealms;
import org.folio.test.types.IntegrationTest;
import org.junit.jupiter.api.Test;

@IntegrationTest
class CredentialIT extends BaseIntegrationTest {

  @Test
  @KeycloakRealms(realms = "/json/keycloak/test-realm.json")
  void createCredentials_positive() throws Exception {
    var credentials = loginCredentials(ADMIN_USERNAME, ADMIN_PASSWORD, null);
    mockMvc.perform(post("/authn/credentials")
        .content(asJsonString(credentials))
        .header(CONTENT_TYPE, APPLICATION_JSON)
        .header(XOkapiHeaders.URL, "http://okapi:9130")
        .header(XOkapiHeaders.TENANT, TENANT))
      .andExpect(status().isCreated());
  }

  @Test
  @KeycloakRealms(realms = "/json/keycloak/test-realm.json")
  void createCredentials_negative_keycloakError() throws Exception {
    var credentials = loginCredentials();
    mockMvc.perform(post("/authn/credentials")
        .content(asJsonString(credentials))
        .header(CONTENT_TYPE, APPLICATION_JSON)
        .header(XOkapiHeaders.URL, "http://okapi:9130")
        .header(XOkapiHeaders.TENANT, TENANT))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.total_records", is(1)))
      .andExpect(jsonPath("$.errors[0].message", is("There already exists credentials for a user: " + USERNAME)))
      .andExpect(jsonPath("$.errors[0].type", is("RequestValidationException")))
      .andExpect(jsonPath("$.errors[0].code", is("validation_error")));
  }

  @Test
  @KeycloakRealms(realms = "/json/keycloak/test-realm.json")
  void createCredentials_negative_alreadyExist() throws Exception {
    var credentials = loginCredentialsWithoutId();
    mockMvc.perform(post("/authn/credentials")
        .content(asJsonString(credentials))
        .header(CONTENT_TYPE, APPLICATION_JSON)
        .header(XOkapiHeaders.URL, "http://okapi:9130")
        .header(XOkapiHeaders.TENANT, TENANT))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.total_records", is(1)))
      .andExpect(jsonPath("$.errors[0].message", is("There already exists credentials for a user: " + USERNAME)))
      .andExpect(jsonPath("$.errors[0].type", is("RequestValidationException")))
      .andExpect(jsonPath("$.errors[0].code", is("validation_error")));
  }

  @Test
  @KeycloakRealms(realms = "/json/keycloak/test-realm.json")
  void deleteCredentials_positive() throws Exception {
    mockMvc.perform(delete("/authn/credentials")
        .param("userId", USER_ID)
        .header(CONTENT_TYPE, APPLICATION_JSON)
        .header(XOkapiHeaders.URL, "http://okapi:9130")
        .header(XOkapiHeaders.TENANT, TENANT))
      .andExpect(status().isNoContent());
  }

  @Test
  void deleteCredentials_negative_keycloakError() throws Exception {
    mockMvc.perform(delete("/authn/credentials")
        .param("userId", USER_ID)
        .header(CONTENT_TYPE, APPLICATION_JSON)
        .header(XOkapiHeaders.URL, "http://okapi:9130")
        .header(XOkapiHeaders.TENANT, TENANT))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.total_records", is(1)))
      .andExpect(jsonPath("$.errors[0].message", is("Failed to delete credentials for a user: " + USER_ID)))
      .andExpect(jsonPath("$.errors[0].type", is("ServiceException")))
      .andExpect(jsonPath("$.errors[0].code", is("service_error")));
  }

  @Test
  @KeycloakRealms(realms = "/json/keycloak/test-realm.json")
  void deleteCredentials_negative_notFound() throws Exception {
    mockMvc.perform(delete("/authn/credentials")
        .param("userId", ADMIN_USER_ID)
        .header(CONTENT_TYPE, APPLICATION_JSON)
        .header(XOkapiHeaders.URL, "http://okapi:9130")
        .header(XOkapiHeaders.TENANT, TENANT))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.total_records", is(1)))
      .andExpect(jsonPath("$.errors[0].message", is("No credentials for userId " + ADMIN_USER_ID + " found")))
      .andExpect(jsonPath("$.errors[0].type", is("EntityNotFoundException")))
      .andExpect(jsonPath("$.errors[0].code", is("not_found_error")));
  }

  @Test
  @KeycloakRealms(realms = "/json/keycloak/test-realm.json")
  void updateCredentials_positive() throws Exception {
    var credentials = updateCredentials();
    mockMvc.perform(post("/authn/update")
        .content(asJsonString(credentials))
        .header(CONTENT_TYPE, APPLICATION_JSON)
        .header(XOkapiHeaders.URL, OKAPI_URL)
        .header(XOkapiHeaders.TENANT, TENANT))
      .andExpect(status().isNoContent());
  }

  @Test
  void updateCredentials_negative_keycloakError() throws Exception {
    var credentials = updateCredentials();
    mockMvc.perform(post("/authn/update")
        .content(asJsonString(credentials))
        .header(CONTENT_TYPE, APPLICATION_JSON)
        .header(XOkapiHeaders.URL, OKAPI_URL)
        .header(XOkapiHeaders.TENANT, TENANT))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.total_records", is(1)))
      .andExpect(jsonPath("$.errors[0].message", is("Failed to update credentials for a user: " + USER_ID)))
      .andExpect(jsonPath("$.errors[0].type", is("ServiceException")))
      .andExpect(jsonPath("$.errors[0].code", is("service_error")));
  }

  @Test
  @KeycloakRealms(realms = "/json/keycloak/test-realm.json")
  void checkCredentials_positive() throws Exception {
    mockMvc.perform(get("/authn/credentials-existence")
        .param("userId", USER_ID)
        .header(CONTENT_TYPE, APPLICATION_JSON)
        .header(XOkapiHeaders.URL, "http://okapi:9130")
        .header(XOkapiHeaders.TENANT, TENANT))
      .andExpect(status().isOk())
      .andExpect(content().json(asJsonString(new CredentialsExistence().credentialsExist(true))));
  }

  @Test
  void checkCredentials_negative_keycloakError() throws Exception {
    mockMvc.perform(get("/authn/credentials-existence")
        .param("userId", USER_ID)
        .header(CONTENT_TYPE, APPLICATION_JSON)
        .header(XOkapiHeaders.URL, "http://okapi:9130")
        .header(XOkapiHeaders.TENANT, TENANT))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.total_records", is(1)))
      .andExpect(jsonPath("$.errors[0].message", is("Failed to get credentials for a user: " + USER_ID)))
      .andExpect(jsonPath("$.errors[0].type", is("ServiceException")))
      .andExpect(jsonPath("$.errors[0].code", is("service_error")));
  }
}
