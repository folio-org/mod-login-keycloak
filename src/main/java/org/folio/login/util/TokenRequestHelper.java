package org.folio.login.util;

import static java.util.Map.entry;
import static org.keycloak.OAuth2Constants.AUTHORIZATION_CODE;
import static org.keycloak.OAuth2Constants.CLIENT_ID;
import static org.keycloak.OAuth2Constants.CLIENT_SECRET;
import static org.keycloak.OAuth2Constants.CODE;
import static org.keycloak.OAuth2Constants.GRANT_TYPE;
import static org.keycloak.OAuth2Constants.REDIRECT_URI;
import static org.keycloak.OAuth2Constants.REFRESH_TOKEN;
import static org.keycloak.OAuth2Constants.USERNAME;

import java.util.Map;
import lombok.experimental.UtilityClass;
import org.folio.login.domain.dto.LoginCredentials;
import org.folio.login.domain.model.KeycloakRealmConfiguration;

@UtilityClass
public class TokenRequestHelper {
  private static final String PASSWORD = "password";

  public static Map<String, String> preparePasswordRequestBody(
    LoginCredentials credentials, KeycloakRealmConfiguration realmConfiguration) {
    return Map.ofEntries(
      entry(GRANT_TYPE, PASSWORD),
      entry(CLIENT_ID, realmConfiguration.getClientId()),
      entry(USERNAME, credentials.getUsername()),
      entry(PASSWORD, credentials.getPassword()),
      entry(CLIENT_SECRET, realmConfiguration.getClientSecret() != null ? realmConfiguration.getClientSecret() : ""));
  }

  public static Map<String, String> prepareCodeRequestBody(String code, String redirectUri,
    KeycloakRealmConfiguration config) {
    return Map.ofEntries(
      entry(GRANT_TYPE, AUTHORIZATION_CODE),
      entry(CLIENT_ID, config.getClientId()),
      entry(CLIENT_SECRET, config.getClientSecret()),
      entry(CODE, code),
      entry(REDIRECT_URI, redirectUri));
  }

  public static Map<String, String> prepareRefreshRequestBody(String refreshToken, KeycloakRealmConfiguration config) {
    return Map.ofEntries(
      entry(GRANT_TYPE, REFRESH_TOKEN),
      entry(CLIENT_ID, config.getClientId()),
      entry(CLIENT_SECRET, config.getClientSecret()),
      entry(REFRESH_TOKEN, refreshToken));
  }
}
