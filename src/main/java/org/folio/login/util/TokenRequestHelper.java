package org.folio.login.util;

import static org.keycloak.OAuth2Constants.AUTHORIZATION_CODE;
import static org.keycloak.OAuth2Constants.CLIENT_ID;
import static org.keycloak.OAuth2Constants.CLIENT_SECRET;
import static org.keycloak.OAuth2Constants.CODE;
import static org.keycloak.OAuth2Constants.GRANT_TYPE;
import static org.keycloak.OAuth2Constants.REDIRECT_URI;
import static org.keycloak.OAuth2Constants.REFRESH_TOKEN;
import static org.keycloak.OAuth2Constants.USERNAME;

import lombok.experimental.UtilityClass;
import org.folio.login.domain.dto.LoginCredentials;
import org.folio.login.domain.model.KeycloakRealmConfiguration;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@UtilityClass
public class TokenRequestHelper {
  private static final String PASSWORD = "password";

  public static MultiValueMap<String, String> preparePasswordRequestBody(
    LoginCredentials credentials, KeycloakRealmConfiguration realmConfiguration) {
    var form = new LinkedMultiValueMap<String, String>();
    form.add(GRANT_TYPE, PASSWORD);
    form.add(CLIENT_ID, realmConfiguration.getClientId());
    form.add(USERNAME, credentials.getUsername());
    form.add(PASSWORD, credentials.getPassword());
    form.add(CLIENT_SECRET, realmConfiguration.getClientSecret() != null ? realmConfiguration.getClientSecret() : "");
    return form;
  }

  public static MultiValueMap<String, String> prepareCodeRequestBody(String code, String redirectUri,
    KeycloakRealmConfiguration config) {
    var form = new LinkedMultiValueMap<String, String>();
    form.add(GRANT_TYPE, AUTHORIZATION_CODE);
    form.add(CLIENT_ID, config.getClientId());
    form.add(CLIENT_SECRET, config.getClientSecret());
    form.add(CODE, code);
    form.add(REDIRECT_URI, redirectUri);
    return form;
  }

  public static MultiValueMap<String, String> prepareRefreshRequestBody(String refreshToken,
    KeycloakRealmConfiguration config) {
    var form = new LinkedMultiValueMap<String, String>();
    form.add(GRANT_TYPE, REFRESH_TOKEN);
    form.add(CLIENT_ID, config.getClientId());
    form.add(CLIENT_SECRET, config.getClientSecret());
    form.add(REFRESH_TOKEN, refreshToken);
    return form;
  }
}
