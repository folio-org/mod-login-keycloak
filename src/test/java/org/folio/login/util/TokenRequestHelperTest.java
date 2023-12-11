package org.folio.login.util;

import static java.util.Map.entry;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.keycloak.OAuth2Constants.AUTHORIZATION_CODE;
import static org.keycloak.OAuth2Constants.CLIENT_ID;
import static org.keycloak.OAuth2Constants.CLIENT_SECRET;
import static org.keycloak.OAuth2Constants.GRANT_TYPE;
import static org.keycloak.OAuth2Constants.PASSWORD;
import static org.keycloak.OAuth2Constants.REFRESH_TOKEN;
import static org.keycloak.OAuth2Constants.USERNAME;

import java.util.Map;
import org.folio.login.domain.dto.LoginCredentials;
import org.folio.login.domain.model.KeycloakRealmConfiguration;
import org.folio.login.support.TestConstants;
import org.junit.jupiter.api.Test;
import org.keycloak.OAuth2Constants;

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
    var actual = TokenRequestHelper.preparePasswordRequestBody(credential, config);

    var expected = Map.ofEntries(
      entry(GRANT_TYPE, PASSWORD),
      entry(CLIENT_ID, TEST_CLIENT),
      entry(CLIENT_SECRET, TEST_CLIENT_SECRET),
      entry(USERNAME, TEST_USER),
      entry(PASSWORD, TEST_USER_PASSWORD)
    );
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
    var actual = TokenRequestHelper.prepareCodeRequestBody(CODE_VALUE, REDIRECT_URI, config);

    var expected = Map.ofEntries(
      entry(GRANT_TYPE, AUTHORIZATION_CODE),
      entry(CLIENT_ID, config.getClientId()),
      entry(CLIENT_SECRET, config.getClientSecret()),
      entry(OAuth2Constants.CODE, CODE_VALUE),
      entry(OAuth2Constants.REDIRECT_URI, REDIRECT_URI)
    );
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
    var actual = TokenRequestHelper.prepareRefreshRequestBody(TestConstants.REFRESH_TOKEN, config);

    var expected = Map.ofEntries(
      entry(GRANT_TYPE, REFRESH_TOKEN),
      entry(CLIENT_ID, config.getClientId()),
      entry(CLIENT_SECRET, config.getClientSecret()),
      entry(REFRESH_TOKEN, TestConstants.REFRESH_TOKEN)
    );
    assertEquals(actual, expected);
  }
}
