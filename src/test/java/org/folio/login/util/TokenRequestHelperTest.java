package org.folio.login.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.keycloak.OAuth2Constants.AUTHORIZATION_CODE;
import static org.keycloak.OAuth2Constants.CLIENT_ID;
import static org.keycloak.OAuth2Constants.CLIENT_SECRET;
import static org.keycloak.OAuth2Constants.GRANT_TYPE;
import static org.keycloak.OAuth2Constants.PASSWORD;
import static org.keycloak.OAuth2Constants.REFRESH_TOKEN;
import static org.keycloak.OAuth2Constants.USERNAME;

import org.folio.login.domain.dto.LoginCredentials;
import org.folio.login.domain.model.KeycloakRealmConfiguration;
import org.folio.login.support.TestConstants;
import org.junit.jupiter.api.Test;
import org.keycloak.OAuth2Constants;
import org.springframework.util.LinkedMultiValueMap;

class TokenRequestHelperTest {

  private static final String TEST_CLIENT = "test-login-app";
  private static final String TEST_CLIENT_SECRET = "secret";
  private static final String TEST_USER = "testuser";
  private static final String TEST_USER_PASSWORD = "pass";
  private static final String REDIRECT_URI = "/*";
  private static final String CODE_VALUE = "foobar";

  @Test
  void preparePasswordRequestBody_positive() {
    var config = new KeycloakRealmConfiguration();
    config.setClientId(TEST_CLIENT);
    config.setClientSecret(TEST_CLIENT_SECRET);

    var credential = new LoginCredentials();
    credential.username(TEST_USER);
    credential.password(TEST_USER_PASSWORD);

    var expected = new LinkedMultiValueMap<String, String>();
    expected.add(GRANT_TYPE, PASSWORD);
    expected.add(CLIENT_ID, TEST_CLIENT);
    expected.add(CLIENT_SECRET, TEST_CLIENT_SECRET);
    expected.add(USERNAME, TEST_USER);
    expected.add(PASSWORD, TEST_USER_PASSWORD);
    var actual = TokenRequestHelper.preparePasswordRequestBody(credential, config);
    assertEquals(actual, expected);
  }

  @Test
  void prepareCodeRequestBody_positive() {
    var config = new KeycloakRealmConfiguration();
    config.setClientId(TEST_CLIENT);
    config.setClientSecret(TEST_CLIENT_SECRET);

    var credential = new LoginCredentials();
    credential.username(TEST_USER);
    credential.password(TEST_USER_PASSWORD);

    var expected = new LinkedMultiValueMap<String, String>();
    expected.add(GRANT_TYPE, AUTHORIZATION_CODE);
    expected.add(CLIENT_ID, config.getClientId());
    expected.add(CLIENT_SECRET, config.getClientSecret());
    expected.add(OAuth2Constants.CODE, CODE_VALUE);
    expected.add(OAuth2Constants.REDIRECT_URI, REDIRECT_URI);
    var actual = TokenRequestHelper.prepareCodeRequestBody(CODE_VALUE, REDIRECT_URI, config);
    assertEquals(actual, expected);
  }

  @Test
  void prepareRefreshRequestBody_positive() {
    var config = new KeycloakRealmConfiguration();
    config.setClientId(TEST_CLIENT);
    config.setClientSecret(TEST_CLIENT_SECRET);

    var credential = new LoginCredentials();
    credential.username(TEST_USER);
    credential.password(TEST_USER_PASSWORD);

    var expected = new LinkedMultiValueMap<String, String>();
    expected.add(GRANT_TYPE, REFRESH_TOKEN);
    expected.add(CLIENT_ID, config.getClientId());
    expected.add(CLIENT_SECRET, config.getClientSecret());
    expected.add(REFRESH_TOKEN, TestConstants.REFRESH_TOKEN);
    var actual = TokenRequestHelper.prepareRefreshRequestBody(TestConstants.REFRESH_TOKEN, config);
    assertEquals(actual, expected);
  }
}
