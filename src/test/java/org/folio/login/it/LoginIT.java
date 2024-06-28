package org.folio.login.it;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.login.service.TokenCookieHeaderManager.FOLIO_ACCESS_TOKEN;
import static org.folio.login.service.TokenCookieHeaderManager.FOLIO_REFRESH_TOKEN;
import static org.folio.login.support.TestConstants.ACCESS_TOKEN;
import static org.folio.login.support.TestConstants.EXPIRES_IN;
import static org.folio.login.support.TestConstants.OKAPI_URL;
import static org.folio.login.support.TestConstants.PASSWORD;
import static org.folio.login.support.TestConstants.REFRESH_EXPIRES_IN;
import static org.folio.login.support.TestConstants.REFRESH_TOKEN;
import static org.folio.login.support.TestConstants.TENANT;
import static org.folio.login.support.TestConstants.USERNAME;
import static org.folio.login.support.TestConstants.USER_ID;
import static org.folio.login.support.TestValues.loginCredentials;
import static org.folio.login.support.TestValues.requestCookie;
import static org.folio.login.support.TestValues.requestCookie1;
import static org.folio.login.support.TestValues.requestCookie2;
import static org.folio.test.TestUtils.asJsonString;
import static org.folio.test.TestUtils.parseResponse;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;
import org.folio.login.domain.dto.LoginCredentials;
import org.folio.login.domain.dto.LoginResponse;
import org.folio.login.service.KeycloakService;
import org.folio.login.support.base.BaseIntegrationTest;
import org.folio.spring.integration.XOkapiHeaders;
import org.folio.test.extensions.KeycloakRealms;
import org.folio.test.types.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserSessionRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultHandler;
import org.springframework.test.web.servlet.ResultMatcher;

@Log4j2
@IntegrationTest
class LoginIT extends BaseIntegrationTest {

  private static final LoginCredentials CREDENTIALS = new LoginCredentials().username(USERNAME).password(PASSWORD);

  @Autowired private Keycloak keycloak;
  @SpyBean private KeycloakService keycloakService;

  @Test
  @KeycloakRealms(realms = "/json/keycloak/test-realm.json")
  void login_positive() throws Exception {
    doLogin();
  }

  @Test
  @KeycloakRealms(realms = "/json/keycloak/test-realm.json")
  void loginWithExpiry_positive() throws Exception {
    doPost("/authn/login-with-expiry", CREDENTIALS)
      .andExpect(header().exists(HttpHeaders.SET_COOKIE))
      .andExpect(header().doesNotExist(XOkapiHeaders.TOKEN))
      .andExpect(cookie().httpOnly(FOLIO_ACCESS_TOKEN, true))
      .andExpect(cookie().httpOnly(FOLIO_REFRESH_TOKEN, true));
  }

  @Test
  @KeycloakRealms(realms = "/json/keycloak/test-realm.json")
  void refresh_positive() throws Exception {
    var tokens = doLogin();

    mockMvc.perform(post("/authn/refresh")
        .header(CONTENT_TYPE, APPLICATION_JSON)
        .header(XOkapiHeaders.USER_ID, USER_ID)
        .header(XOkapiHeaders.URL, OKAPI_URL)
        .header(XOkapiHeaders.TENANT, TENANT)
        .cookie(new Cookie(FOLIO_ACCESS_TOKEN, tokens.getOkapiToken()),
          new Cookie(FOLIO_REFRESH_TOKEN, tokens.getRefreshToken())))
      .andExpect(status().isCreated())
      .andExpect(header().doesNotExist(XOkapiHeaders.TOKEN))
      .andExpect(header().exists(HttpHeaders.SET_COOKIE))
      .andExpect(cookie().httpOnly(FOLIO_ACCESS_TOKEN, true))
      .andExpect(cookie().httpOnly(FOLIO_REFRESH_TOKEN, true));
  }

  @Test
  @KeycloakRealms(realms = "/json/keycloak/test-realm.json")
  void logout_positive() throws Exception {
    final var firstLoginTokens = doLogin();
    final var secondLoginTokens = doLogin();
    assertThat(loadKcUserSessions()).isNotEmpty();

    doLogout(firstLoginTokens);
    // logging-out already logged-out session
    doLogout(firstLoginTokens);
    assertThat(loadKcUserSessions()).isNotEmpty();

    // logging-out second session
    doLogout(secondLoginTokens);
    assertThat(loadKcUserSessions()).isEmpty();

    verify(keycloakService, times(2)).logout(firstLoginTokens.getRefreshToken());
    verify(keycloakService).logout(secondLoginTokens.getRefreshToken());
  }

  @Test
  @KeycloakRealms(realms = "/json/keycloak/test-realm.json")
  void logout_positive_noCookieHeader() throws Exception {
    doLogin();
    assertThat(loadKcUserSessions()).isNotEmpty();

    var testCookie1 = requestCookie1();
    var testCookie2 = requestCookie2();

    mockMvc.perform(post("/authn/logout")
        .header(CONTENT_TYPE, APPLICATION_JSON)
        .header(XOkapiHeaders.USER_ID, USER_ID)
        .header(XOkapiHeaders.URL, OKAPI_URL)
        .header(XOkapiHeaders.TENANT, TENANT)
        .cookie(testCookie1, testCookie2))
      .andExpect(status().isNoContent())
      .andExpectAll(invalidatedCookie(FOLIO_ACCESS_TOKEN))
      .andExpectAll(invalidatedCookie(FOLIO_REFRESH_TOKEN))
      .andExpectAll(invalidatedCookie(testCookie1))
      .andExpectAll(invalidatedCookie(testCookie2));

    assertThat(loadKcUserSessions()).isEmpty();
    verify(keycloakService).logoutAll();
  }

  @Test
  @KeycloakRealms(realms = "/json/keycloak/test-realm.json")
  void logoutAll_positive() throws Exception {
    doLogin();
    doLogin();
    assertThat(loadKcUserSessions()).isNotEmpty().hasSize(2);

    var testCookie1 = requestCookie1();
    var testCookie2 = requestCookie2();

    mockMvc.perform(post("/authn/logout-all")
        .header(CONTENT_TYPE, APPLICATION_JSON)
        .header(XOkapiHeaders.USER_ID, USER_ID)
        .header(XOkapiHeaders.URL, OKAPI_URL)
        .header(XOkapiHeaders.TENANT, TENANT)
        .cookie(testCookie1, testCookie2))
      .andExpect(status().isNoContent())
      .andExpectAll(invalidatedCookie(FOLIO_ACCESS_TOKEN))
      .andExpectAll(invalidatedCookie(FOLIO_REFRESH_TOKEN))
      .andExpectAll(invalidatedCookie(testCookie1))
      .andExpectAll(invalidatedCookie(testCookie2));

    assertThat(loadKcUserSessions()).isEmpty();
  }

  @Test
  void login_negative_keycloakError() throws Exception {
    mockMvc.perform(post("/authn/login")
        .content(asJsonString(loginCredentials()))
        .header(CONTENT_TYPE, APPLICATION_JSON)
        .header(XOkapiHeaders.URL, OKAPI_URL)
        .header(XOkapiHeaders.TENANT, TENANT))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.total_records", is(1)))
      .andExpect(jsonPath("$.errors[0].message", is("Failed to obtain a token")))
      .andExpect(jsonPath("$.errors[0].type", is("ServiceException")))
      .andExpect(jsonPath("$.errors[0].code", is("service_error")));
  }

  @Test
  void logout_negative_keycloakErrorAndCookiesInvalidated() throws Exception {
    var refreshCookie = requestCookie(FOLIO_REFRESH_TOKEN, REFRESH_TOKEN, (int) REFRESH_EXPIRES_IN);
    var accessCookie = requestCookie(FOLIO_ACCESS_TOKEN, ACCESS_TOKEN, (int) EXPIRES_IN);
    var testCookie1 = requestCookie1();
    var testCookie2 = requestCookie2();

    mockMvc.perform(post("/authn/logout")
        .header(CONTENT_TYPE, APPLICATION_JSON)
        .header(XOkapiHeaders.USER_ID, USER_ID)
        .header(XOkapiHeaders.URL, OKAPI_URL)
        .header(XOkapiHeaders.TENANT, TENANT)
        .cookie(refreshCookie, accessCookie, testCookie1, testCookie2))
      .andDo(logResponse())
      .andExpect(status().isUnprocessableEntity())
      .andExpect(jsonPath("$.total_records", is(1)))
      .andExpect(jsonPath("$.errors[0].message", is("Authorization server unable to process token logout request")))
      .andExpect(jsonPath("$.errors[0].type", is("TokenLogoutException")))
      .andExpect(jsonPath("$.errors[0].code", is("token.logout.unprocessable")))
      .andExpectAll(invalidatedCookie(refreshCookie))
      .andExpectAll(invalidatedCookie(accessCookie))
      .andExpectAll(invalidatedCookie(testCookie1))
      .andExpectAll(invalidatedCookie(testCookie2));
  }

  @Test
  void logoutAll_negative_keycloakErrorAndCookiesInvalidated() throws Exception {
    var refreshCookie = requestCookie(FOLIO_REFRESH_TOKEN, REFRESH_TOKEN, (int) REFRESH_EXPIRES_IN);
    var accessCookie = requestCookie(FOLIO_ACCESS_TOKEN, ACCESS_TOKEN, (int) EXPIRES_IN);
    var testCookie1 = requestCookie1();
    var testCookie2 = requestCookie2();

    mockMvc.perform(post("/authn/logout-all")
        .header(CONTENT_TYPE, APPLICATION_JSON)
        .header(XOkapiHeaders.USER_ID, USER_ID)
        .header(XOkapiHeaders.URL, OKAPI_URL)
        .header(XOkapiHeaders.TENANT, TENANT)
        .cookie(refreshCookie, accessCookie, testCookie1, testCookie2))
      .andDo(logResponse())
      .andExpect(status().isUnprocessableEntity())
      .andExpect(jsonPath("$.total_records", is(1)))
      .andExpect(jsonPath("$.errors[0].message", is("Authorization server unable to process token logout request")))
      .andExpect(jsonPath("$.errors[0].type", is("TokenLogoutException")))
      .andExpect(jsonPath("$.errors[0].code", is("token.logout.unprocessable")))
      .andExpectAll(invalidatedCookie(refreshCookie))
      .andExpectAll(invalidatedCookie(accessCookie))
      .andExpectAll(invalidatedCookie(testCookie1))
      .andExpectAll(invalidatedCookie(testCookie2));
  }

  @Test
  void logoutAll_negative_emptyTenantAndCookiesInvalidated() throws Exception {
    var refreshCookie = requestCookie(FOLIO_REFRESH_TOKEN, REFRESH_TOKEN, (int) REFRESH_EXPIRES_IN);
    var accessCookie = requestCookie(FOLIO_ACCESS_TOKEN, ACCESS_TOKEN, (int) EXPIRES_IN);
    var testCookie1 = requestCookie1();
    var testCookie2 = requestCookie2();

    mockMvc.perform(post("/authn/logout-all")
        .header(CONTENT_TYPE, APPLICATION_JSON)
        .header(XOkapiHeaders.USER_ID, USER_ID)
        .header(XOkapiHeaders.URL, OKAPI_URL)
        .cookie(refreshCookie, accessCookie, testCookie1, testCookie2))
      .andDo(logResponse())
      .andExpect(status().isBadRequest())
      .andExpect(content().string(containsString("x-okapi-tenant header must be provided")))
      .andExpectAll(invalidatedCookie(refreshCookie))
      .andExpectAll(invalidatedCookie(accessCookie))
      .andExpectAll(invalidatedCookie(testCookie1))
      .andExpectAll(invalidatedCookie(testCookie2));
  }

  @Test
  void token_negative_keycloakErrorAndCookiesInvalidated() throws Exception {
    var testCookie1 = requestCookie1();
    var testCookie2 = requestCookie2();

    var code = "secret_code";
    var redirect = "localhost";

    mockMvc.perform(get("/authn/token")
        .contentType(APPLICATION_JSON)
        .header(XOkapiHeaders.TENANT, TENANT)
        .queryParam("code", code)
        .queryParam("redirect-uri", redirect)
        .cookie(testCookie1, testCookie2))
      .andDo(logResponse())
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.total_records", is(1)))
      .andExpect(jsonPath("$.errors[0].message", is("Failed to obtain a token")))
      .andExpect(jsonPath("$.errors[0].type", is("ServiceException")))
      .andExpect(jsonPath("$.errors[0].code", is("service_error")))
      .andExpectAll(invalidatedCookie(testCookie1))
      .andExpectAll(invalidatedCookie(testCookie2));
  }

  private List<UserSessionRepresentation> loadKcUserSessions() {
    var user = keycloak.realm("test").users().searchByUsername(USERNAME, true).get(0);
    return keycloak.realm("test").users().get(user.getId()).getUserSessions();
  }

  private static LoginResponse doLogin() throws Exception {
    var mvcResult = doPost("/authn/login", CREDENTIALS)
      .andExpect(header().doesNotExist(XOkapiHeaders.TOKEN))
      .andExpect(header().exists(HttpHeaders.SET_COOKIE))
      .andReturn();
    return parseResponse(mvcResult, LoginResponse.class);
  }

  private static void doLogout(LoginResponse tokens) throws Exception {
    mockMvc.perform(post("/authn/logout")
        .header(CONTENT_TYPE, APPLICATION_JSON)
        .header(XOkapiHeaders.USER_ID, USER_ID)
        .header(XOkapiHeaders.URL, OKAPI_URL)
        .header(XOkapiHeaders.TENANT, TENANT)
        .cookie(new Cookie(FOLIO_ACCESS_TOKEN, tokens.getOkapiToken()),
          new Cookie(FOLIO_REFRESH_TOKEN, tokens.getRefreshToken())))
      .andExpect(status().isNoContent())
      .andExpect(cookie().httpOnly(FOLIO_ACCESS_TOKEN, true))
      .andExpect(cookie().httpOnly(FOLIO_REFRESH_TOKEN, true));
  }

  private static ResultActions doPost(String endpoint, Object payload) throws Exception {
    return mockMvc.perform(post(endpoint)
        .content(asJsonString(payload))
        .header(CONTENT_TYPE, APPLICATION_JSON)
        .header(XOkapiHeaders.USER_ID, USER_ID)
        .header(XOkapiHeaders.URL, OKAPI_URL)
        .header(XOkapiHeaders.TENANT, TENANT))
      .andExpect(status().isCreated());
  }

  private static ResultMatcher[] invalidatedCookie(Cookie cookie) {
    return invalidatedCookie(cookie.getName());
  }

  private static ResultMatcher[] invalidatedCookie(String cookieName) {
    ResultMatcher[] result = new ResultMatcher[4];
    result[0] = cookie().value(cookieName, is(emptyString()));
    result[1] = cookie().maxAge(cookieName, 0);
    result[2] = cookie().httpOnly(cookieName, true);
    result[3] = cookie().secure(cookieName, true);

    return result;
  }

  protected static ResultHandler logResponse() {
    return result -> {
      var response = result.getResponse();
      log.info("\n[Headers]\n\t{}", response.getHeaderNames().stream()
        .flatMap(name -> response.getHeaders(name).stream().map(value -> name + ": " + value))
        .collect(Collectors.joining("\n\t"))
      );
      log.info("\n[Res-Body] {}", response.getContentAsString());
    };
  }
}
