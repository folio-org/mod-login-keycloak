package org.folio.login.support;

import static java.util.Map.entry;
import static org.folio.login.support.TestConstants.ACCESS_TOKEN;
import static org.folio.login.support.TestConstants.ACCESS_TOKEN_EXPIRATION_DATE;
import static org.folio.login.support.TestConstants.CLIENT_ID;
import static org.folio.login.support.TestConstants.CLIENT_SECRET;
import static org.folio.login.support.TestConstants.EXPIRATION_TIME;
import static org.folio.login.support.TestConstants.EXPIRES_IN;
import static org.folio.login.support.TestConstants.NEW_PASSWORD;
import static org.folio.login.support.TestConstants.PASSWORD;
import static org.folio.login.support.TestConstants.PASSWORD_RESET_ACTION_ID;
import static org.folio.login.support.TestConstants.PASSWORD_RESET_ACTION_UUID;
import static org.folio.login.support.TestConstants.REFRESH_EXPIRES_IN;
import static org.folio.login.support.TestConstants.REFRESH_TOKEN;
import static org.folio.login.support.TestConstants.REFRESH_TOKEN_EXPIRATION_DATE;
import static org.folio.login.support.TestConstants.USERNAME;
import static org.folio.login.support.TestConstants.USER_CREDENTIAL_ID;
import static org.folio.login.support.TestConstants.USER_ID;
import static org.folio.login.support.TestConstants.USER_UUID;

import java.util.Map;
import org.folio.login.domain.dto.CredentialsExistence;
import org.folio.login.domain.dto.LoginCredentials;
import org.folio.login.domain.dto.LoginResponse;
import org.folio.login.domain.dto.LoginResponseWithExpiry;
import org.folio.login.domain.dto.PasswordCreateAction;
import org.folio.login.domain.dto.PasswordResetAction;
import org.folio.login.domain.dto.ResponseCreateAction;
import org.folio.login.domain.dto.ResponseResetAction;
import org.folio.login.domain.dto.UpdateCredentials;
import org.folio.login.domain.entity.PasswordCreateActionEntity;
import org.folio.login.domain.model.KeycloakAuthentication;
import org.folio.login.domain.model.KeycloakRealmConfiguration;
import org.folio.login.domain.model.PasswordCredential;
import org.folio.login.domain.model.Token;
import org.folio.login.domain.model.TokenContainer;
import org.folio.login.domain.model.UserCredentials;
import org.keycloak.OAuth2Constants;
import org.springframework.http.ResponseCookie;

public class TestValues {

  public static LoginCredentials loginCredentials() {
    return loginCredentials(USERNAME, PASSWORD, USER_ID);
  }

  public static LoginCredentials loginCredentials(String username, String password, String userId) {
    return new LoginCredentials().username(username).password(password).userId(userId);
  }

  public static LoginCredentials loginCredentialsWithoutId() {
    return loginCredentials(USERNAME, PASSWORD, null);
  }

  public static LoginCredentials invalidLoginCredentials() {
    return new LoginCredentials().username(USERNAME).password("invalid").userId(USER_ID);
  }

  public static UpdateCredentials updateCredentials() {
    return new UpdateCredentials().password(PASSWORD).username(USERNAME).userId(USER_ID).newPassword(NEW_PASSWORD);
  }

  public static PasswordCredential createPassword() {
    return PasswordCredential.of(false, "password", PASSWORD);
  }

  public static PasswordCredential updatePassword() {
    return PasswordCredential.of(false, "password", NEW_PASSWORD);
  }

  public static UserCredentials userCredentials() {
    return UserCredentials.of(USER_CREDENTIAL_ID, "password");
  }

  public static KeycloakRealmConfiguration keycloakRealmConfiguration() {
    return new KeycloakRealmConfiguration()
      .clientId(CLIENT_ID)
      .clientSecret(CLIENT_SECRET);
  }

  public static LoginResponse loginResponse() {
    return new LoginResponse().okapiToken(ACCESS_TOKEN).refreshToken(REFRESH_TOKEN);
  }

  public static LoginResponseWithExpiry loginResponseWithExpiry() {
    return new LoginResponseWithExpiry().accessTokenExpiration(ACCESS_TOKEN_EXPIRATION_DATE)
      .refreshTokenExpiration(REFRESH_TOKEN_EXPIRATION_DATE);
  }

  public static KeycloakAuthentication keycloakAuthentication() {
    return KeycloakAuthentication.of(ACCESS_TOKEN, REFRESH_TOKEN, EXPIRES_IN, REFRESH_EXPIRES_IN);
  }

  public static Map<String, String> loginRequest(String username, String password,
    String clientId,
    String clientSecret) {
    return Map.ofEntries(
      entry("grant_type", "password"),
      entry("username", username),
      entry("password", password),
      entry("client_id", clientId),
      entry("client_secret", clientSecret));
  }

  public static Map<String, String> loginRequest(String username, String password, String clientId) {
    return Map.ofEntries(
      entry("grant_type", "password"),
      entry("username", username),
      entry("password", password),
      entry("client_id", clientId),
      entry("client_secret", ""));
  }

  public static Map<String, String> loginRequestAuthCode(String code, String clientId,
    String clientSecret, String redirectUri) {
    return Map.ofEntries(
      entry("grant_type", "authorization_code"),
      entry("code", code),
      entry("redirect_uri", redirectUri),
      entry("client_id", clientId),
      entry("client_secret", clientSecret));
  }

  public static Map<String, String> refreshTokenRequest(String refreshToken) {
    return Map.ofEntries(
      entry("grant_type", OAuth2Constants.REFRESH_TOKEN),
      entry("refresh_token", refreshToken),
      entry("client_id", CLIENT_ID),
      entry("client_secret", CLIENT_SECRET));
  }

  public static PasswordResetAction passwordResetAction() {
    return new PasswordResetAction().passwordResetActionId(PASSWORD_RESET_ACTION_ID).newPassword(NEW_PASSWORD);
  }

  public static PasswordCreateAction passwordCreateAction() {
    return new PasswordCreateAction().id(PASSWORD_RESET_ACTION_ID).userId(USER_ID).expirationTime(EXPIRATION_TIME);
  }

  public static PasswordCreateAction passwordCreateAction(String userId) {
    return new PasswordCreateAction().id(PASSWORD_RESET_ACTION_ID).userId(userId).expirationTime(EXPIRATION_TIME);
  }

  public static ResponseCreateAction responseCreateAction(boolean passwordExists) {
    return new ResponseCreateAction().passwordExists(passwordExists);
  }

  public static ResponseResetAction responseResetAction(boolean isNewPassword) {
    return new ResponseResetAction().isNewPassword(isNewPassword);
  }

  public static CredentialsExistence credentialsExistence(boolean credentialExist) {
    return new CredentialsExistence().credentialsExist(credentialExist);
  }

  public static PasswordCreateActionEntity passwordCreateActionEntity() {
    var entity = new PasswordCreateActionEntity();
    entity.setId(PASSWORD_RESET_ACTION_UUID);
    entity.setUserId(USER_UUID);
    entity.setExpirationTime(EXPIRATION_TIME);
    return entity;
  }

  public static Token accessToken() {
    return Token.builder()
      .jwt(ACCESS_TOKEN)
      .expiresIn(EXPIRES_IN)
      .expirationDate(ACCESS_TOKEN_EXPIRATION_DATE)
      .build();
  }

  public static Token refreshToken() {
    return Token.builder()
      .jwt(REFRESH_TOKEN)
      .expiresIn(REFRESH_EXPIRES_IN)
      .expirationDate(REFRESH_TOKEN_EXPIRATION_DATE)
      .build();
  }

  public static TokenContainer tokenContainer() {
    return TokenContainer.builder().accessToken(accessToken()).refreshToken(refreshToken()).build();
  }

  public static String responseCookie(String name, String value, long expires) {
    return ResponseCookie.from(name, value)
      .httpOnly(true)
      .secure(true)
      .path("/")
      .maxAge(expires)
      .build()
      .toString();
  }
}
