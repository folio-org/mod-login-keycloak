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
import static org.folio.login.support.TestValues.requestCookie;
import static org.folio.login.support.TestValues.requestCookie1;
import static org.folio.login.support.TestValues.requestCookie2;
import static org.folio.login.support.TestValues.responseCookie;
import static org.folio.login.support.TestValues.tokenContainer;
import static org.folio.spring.integration.XOkapiHeaders.TOKEN;
import static org.folio.test.TestUtils.asJsonString;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.SET_COOKIE;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
import org.folio.login.configuration.property.TokenHeaderProperties;
import org.folio.login.controller.cookie.InvalidateCookiesResponseBodyAdvice;
import org.folio.login.domain.dto.LoginCredentials;
import org.folio.login.exception.TokenLogoutException;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

@UnitTest
@Import({ApiExceptionHandler.class, InvalidateCookiesResponseBodyAdvice.class})
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
  void token_positive_requestCookiesNotInvalidated() throws Exception {
    var tokenContainer = tokenContainer();
    var loginResponse = loginResponseWithExpiry();
    var code = "secret_code";
    var redirect = "localhost";
    var testCookie1 = requestCookie1();
    var testCookie2 = requestCookie2();

    when(tokenHeaderProperties.getEnabled()).thenReturn(FALSE);
    when(loginService.token(code, redirect, null, null))
      .thenReturn(tokenContainer);

    mockMvc.perform(get("/authn/token")
        .contentType(APPLICATION_JSON)
        .header(XOkapiHeaders.TENANT, "test-tenant")
        .queryParam("code", code)
        .queryParam("redirect-uri", redirect)
        .cookie(testCookie1, testCookie2))
      .andExpect(status().isCreated())
      .andExpect(header().doesNotExist(XOkapiHeaders.TOKEN))
      .andExpectAll(cookie().exists(FOLIO_REFRESH_TOKEN), cookie().exists(FOLIO_ACCESS_TOKEN))
      .andExpectAll(cookie().doesNotExist(testCookie1.getName()), cookie().doesNotExist(testCookie2.getName()))
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
  void token_negative_requestCookiesInvalidated() throws Exception {
    var errorMessage = "Failed to get value from secure store [tenantId: test-tenant, key: test-tenant-login-app]";
    var code = "secret_code";
    var redirect = "localhost";
    var testCookie1 = requestCookie1();
    var testCookie2 = requestCookie2();

    when(loginService.token(code, "localhost", null, null))
      .thenThrow(new IllegalStateException(errorMessage));

    mockMvc.perform(get("/authn/token")
        .contentType(APPLICATION_JSON)
        .header(XOkapiHeaders.TENANT, "test-tenant")
        .queryParam("redirect-uri", redirect)
        .queryParam("code", code)
        .cookie(testCookie1, testCookie2))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.total_records", is(1)))
      .andExpect(jsonPath("$.errors[0].message", is(errorMessage)))
      .andExpect(jsonPath("$.errors[0].type", is("IllegalStateException")))
      .andExpect(jsonPath("$.errors[0].code", is("service_error")))
      .andExpectAll(invalidatedCookie(testCookie1))
      .andExpectAll(invalidatedCookie(testCookie2));
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

  @Test
  void logout_positive_requestCookiesInvalidated() throws Exception {
    doNothing().when(loginService).logout(REFRESH_TOKEN);

    var refreshCookie = requestCookie(FOLIO_REFRESH_TOKEN, REFRESH_TOKEN, (int) REFRESH_EXPIRES_IN);
    var testCookie1 = requestCookie1();
    var testCookie2 = requestCookie2();

    mockMvc.perform(post("/authn/logout")
        .contentType(APPLICATION_JSON)
        .header(XOkapiHeaders.TENANT, "test-tenant")
        .cookie(refreshCookie, testCookie1, testCookie2))
      .andExpect(status().isNoContent())
      .andExpectAll(invalidatedCookie(refreshCookie))
      .andExpectAll(invalidatedCookie(testCookie1))
      .andExpectAll(invalidatedCookie(testCookie2));
  }

  @Test
  void logout_negative_requestCookiesInvalidated() throws Exception {
    doThrow(new TokenLogoutException("Token logout failed", new RuntimeException()))
      .when(loginService).logout(REFRESH_TOKEN);

    var refreshCookie = requestCookie(FOLIO_REFRESH_TOKEN, REFRESH_TOKEN, (int) REFRESH_EXPIRES_IN);
    var testCookie1 = requestCookie1();
    var testCookie2 = requestCookie2();

    mockMvc.perform(post("/authn/logout")
        .contentType(APPLICATION_JSON)
        .header(XOkapiHeaders.TENANT, "test-tenant")
        .cookie(refreshCookie, testCookie1, testCookie2))
      .andExpect(status().isUnprocessableEntity())
      .andExpectAll(invalidatedCookie(refreshCookie))
      .andExpectAll(invalidatedCookie(testCookie1))
      .andExpectAll(invalidatedCookie(testCookie2));
  }

  @Test
  void logoutAll_positive_requestCookiesInvalidated() throws Exception {
    doNothing().when(loginService).logoutAll();

    var refreshCookie = requestCookie(FOLIO_REFRESH_TOKEN, REFRESH_TOKEN, (int) REFRESH_EXPIRES_IN);
    var testCookie1 = requestCookie1();
    var testCookie2 = requestCookie2();

    mockMvc.perform(post("/authn/logout-all")
        .contentType(APPLICATION_JSON)
        .header(XOkapiHeaders.TENANT, "test-tenant")
        .cookie(refreshCookie, testCookie1, testCookie2))
      .andExpect(status().isNoContent())
      .andExpectAll(invalidatedCookie(refreshCookie))
      .andExpectAll(invalidatedCookie(testCookie1))
      .andExpectAll(invalidatedCookie(testCookie2));
  }

  @Test
  void logoutAll_negative_requestCookiesInvalidated() throws Exception {
    doThrow(new TokenLogoutException("Token logout failed", new RuntimeException()))
      .when(loginService).logoutAll();

    var refreshCookie = requestCookie(FOLIO_REFRESH_TOKEN, REFRESH_TOKEN, (int) REFRESH_EXPIRES_IN);
    var testCookie1 = requestCookie1();
    var testCookie2 = requestCookie2();

    mockMvc.perform(post("/authn/logout-all")
        .contentType(APPLICATION_JSON)
        .header(XOkapiHeaders.TENANT, "test-tenant")
        .cookie(refreshCookie, testCookie1, testCookie2))
      .andExpect(status().isUnprocessableEntity())
      .andExpectAll(invalidatedCookie(refreshCookie))
      .andExpectAll(invalidatedCookie(testCookie1))
      .andExpectAll(invalidatedCookie(testCookie2));
  }

  private static ResultMatcher[] invalidatedCookie(Cookie cookie) {
    ResultMatcher[] result = new ResultMatcher[2];
    result[0] = cookie().value(cookie.getName(), is(emptyString()));
    result[1] = cookie().maxAge(cookie.getName(), 0);

    return result;
  }
}
