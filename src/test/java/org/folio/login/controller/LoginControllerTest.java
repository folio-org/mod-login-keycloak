package org.folio.login.controller;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.folio.login.service.TokenCookieHeaderManager.FOLIO_ACCESS_TOKEN;
import static org.folio.login.service.TokenCookieHeaderManager.FOLIO_REFRESH_TOKEN;
import static org.folio.login.support.TestConstants.ACCESS_TOKEN;
import static org.folio.login.support.TestConstants.EXPIRES_IN;
import static org.folio.login.support.TestConstants.REFRESH_EXPIRES_IN;
import static org.folio.login.support.TestConstants.REFRESH_TOKEN;
import static org.folio.login.support.TestValues.loginResponse;
import static org.folio.login.support.TestValues.loginResponseWithExpiry;
import static org.folio.login.support.TestValues.responseCookie;
import static org.folio.login.support.TestValues.tokenContainer;
import static org.folio.spring.integration.XOkapiHeaders.TOKEN;
import static org.folio.test.TestUtils.asJsonString;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.SET_COOKIE;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.folio.login.configuration.property.TokenHeaderProperties;
import org.folio.login.domain.dto.LoginCredentials;
import org.folio.login.service.LoginService;
import org.folio.login.service.TokenCookieHeaderManager;
import org.folio.spring.integration.XOkapiHeaders;
import org.folio.test.types.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.test.web.servlet.MockMvc;

@UnitTest
@MockBean(KafkaAdmin.class)
@Import(ApiExceptionHandler.class)
@WebMvcTest(LoginController.class)
class LoginControllerTest {

  @Autowired private MockMvc mockMvc;
  @MockBean private LoginService loginService;
  @MockBean private TokenHeaderProperties tokenHeaderProperties;
  @MockBean private TokenCookieHeaderManager tokenCookieHeaderManager;

  @BeforeEach
  void setUp() {
    var headers = new HttpHeaders();
    headers.add(SET_COOKIE, responseCookie(FOLIO_ACCESS_TOKEN, ACCESS_TOKEN, EXPIRES_IN));
    headers.add(SET_COOKIE, responseCookie(FOLIO_REFRESH_TOKEN, REFRESH_TOKEN, REFRESH_EXPIRES_IN));
    when(tokenCookieHeaderManager.createAuthorizationCookieHeader(tokenContainer()))
      .thenReturn(headers);
  }

  @Test
  void login_positive() throws Exception {
    var tokenContainer = tokenContainer();
    var loginResponse = loginResponse();
    var loginCredentials = new LoginCredentials().username("test").password("pwd");

    when(tokenHeaderProperties.getEnabled()).thenReturn(FALSE);
    when(loginService.login(loginCredentials, null, null)).thenReturn(tokenContainer);

    mockMvc.perform(post("/authn/login")
        .contentType(APPLICATION_JSON)
        .header(XOkapiHeaders.TENANT, "test-tenant")
        .content(asJsonString(loginCredentials)))
      .andExpect(status().isCreated())
      .andExpect(header().doesNotExist(XOkapiHeaders.TOKEN))
      .andExpect(header().exists(HttpHeaders.SET_COOKIE))
      .andExpect(content().json(asJsonString(loginResponse)));
  }

  @Test
  void login_positive_withTokenHeader() throws Exception {
    var loginCredentials = new LoginCredentials().username("test").password("pwd");
    var tokenContainer = tokenContainer();
    var loginResponse = loginResponse();

    when(tokenHeaderProperties.getEnabled()).thenReturn(TRUE);
    when(loginService.login(loginCredentials, null, null)).thenReturn(tokenContainer);

    mockMvc.perform(post("/authn/login")
        .contentType(APPLICATION_JSON)
        .header(XOkapiHeaders.TENANT, "test-tenant")
        .content(asJsonString(loginCredentials)))
      .andExpect(status().isCreated())
      .andExpect(header().exists(TOKEN))
      .andExpect(header().exists(SET_COOKIE))
      .andExpect(content().json(asJsonString(loginResponse)));
  }

  @Test
  void login_negative_keycloakConfigNotFound() throws Exception {
    var loginCredentials = new LoginCredentials().username("test").password("pwd");
    var errorMessage = "Failed to get value from secure store [tenantId: test-tenant, key: test-tenant-login-app]";
    when(loginService.login(loginCredentials, null, null)).thenThrow(new IllegalStateException(errorMessage));

    mockMvc.perform(post("/authn/login")
        .contentType(APPLICATION_JSON)
        .header(XOkapiHeaders.TENANT, "test-tenant")
        .content(asJsonString(loginCredentials)))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.total_records", is(1)))
      .andExpect(jsonPath("$.errors[0].message", is(errorMessage)))
      .andExpect(jsonPath("$.errors[0].type", is("IllegalStateException")))
      .andExpect(jsonPath("$.errors[0].code", is("service_error")));
  }

  @Test
  void token_positive() throws Exception {
    var tokenContainer = tokenContainer();
    var loginResponse = loginResponseWithExpiry();
    var code = "secret_code";
    var redirect = "localhost";

    when(tokenHeaderProperties.getEnabled()).thenReturn(FALSE);
    when(loginService.token(code, redirect, null, null))
      .thenReturn(tokenContainer);

    mockMvc.perform(get("/authn/token")
        .contentType(APPLICATION_JSON)
        .header(XOkapiHeaders.TENANT, "test-tenant")
        .queryParam("code", code)
        .queryParam("redirect-uri", redirect))
      .andExpect(status().isCreated())
      .andExpect(header().doesNotExist(XOkapiHeaders.TOKEN))
      .andExpect(header().exists(HttpHeaders.SET_COOKIE))
      .andExpect(content().json(asJsonString(loginResponse)));
  }

  @Test
  void token_positive_withoutTokenHeader() throws Exception {
    var tokenContainer = tokenContainer();
    var loginResponse = loginResponseWithExpiry();

    var code = "secret_code";
    var redirect = "localhost";

    when(tokenHeaderProperties.getEnabled()).thenReturn(TRUE);
    when(loginService.token(code, redirect, null, null)).thenReturn(tokenContainer);

    mockMvc.perform(get("/authn/token").contentType(APPLICATION_JSON).header(XOkapiHeaders.TENANT, "test-tenant")
        .queryParam("code", code).queryParam("redirect-uri", redirect)).andExpect(status().isCreated())
      .andExpect(header().exists(TOKEN)).andExpect(header().exists(SET_COOKIE))
      .andExpect(content().json(asJsonString(loginResponse)));
  }

  @Test
  void token_negative_keycloakConfigNotFound() throws Exception {
    var errorMessage = "Failed to get value from secure store [tenantId: test-tenant, key: test-tenant-login-app]";
    var code = "secret_code";
    var redirect = "localhost";

    when(loginService.token(code, "localhost", null, null))
      .thenThrow(new IllegalStateException(errorMessage));

    mockMvc.perform(get("/authn/token")
        .contentType(APPLICATION_JSON)
        .header(XOkapiHeaders.TENANT, "test-tenant")
        .queryParam("redirect-uri", redirect)
        .queryParam("code", code))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.total_records", is(1)))
      .andExpect(jsonPath("$.errors[0].message", is(errorMessage)))
      .andExpect(jsonPath("$.errors[0].type", is("IllegalStateException")))
      .andExpect(jsonPath("$.errors[0].code", is("service_error")));
  }

  @Test
  void loginWithExpiry_positive() throws Exception {
    var tokenContainer = tokenContainer();
    var response = loginResponseWithExpiry();
    var loginCredentials = new LoginCredentials().username("test").password("pwd");

    when(loginService.login(loginCredentials, null, null)).thenReturn(tokenContainer);

    mockMvc.perform(post("/authn/login-with-expiry")
        .contentType(APPLICATION_JSON)
        .header(XOkapiHeaders.TENANT, "test-tenant")
        .content(asJsonString(loginCredentials)))
      .andExpect(status().isCreated())
      .andExpect(header().doesNotExist(XOkapiHeaders.TOKEN))
      .andExpect(header().exists(HttpHeaders.SET_COOKIE))
      .andExpect(content().json(asJsonString(response)));
  }
}
