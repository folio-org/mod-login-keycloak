package org.folio.login.service;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.folio.login.support.TestConstants.ACCESS_TOKEN;
import static org.folio.login.support.TestConstants.AUTH_CODE;
import static org.folio.login.support.TestConstants.BEARER_TOKEN;
import static org.folio.login.support.TestConstants.CLIENT_ID;
import static org.folio.login.support.TestConstants.CLIENT_SECRET;
import static org.folio.login.support.TestConstants.PASSWORD;
import static org.folio.login.support.TestConstants.REALM;
import static org.folio.login.support.TestConstants.REFRESH_TOKEN;
import static org.folio.login.support.TestConstants.TENANT;
import static org.folio.login.support.TestConstants.USERNAME;
import static org.folio.login.support.TestConstants.USER_CREDENTIAL_ID;
import static org.folio.login.support.TestConstants.USER_ID;
import static org.folio.login.support.TestConstants.USER_UUID;
import static org.folio.login.support.TestValues.createPassword;
import static org.folio.login.support.TestValues.keycloakAuthentication;
import static org.folio.login.support.TestValues.keycloakRealmConfiguration;
import static org.folio.login.support.TestValues.loginCredentials;
import static org.folio.login.support.TestValues.loginCredentialsWithoutId;
import static org.folio.login.support.TestValues.loginRequest;
import static org.folio.login.support.TestValues.loginRequestAuthCode;
import static org.folio.login.support.TestValues.passwordResetAction;
import static org.folio.login.support.TestValues.refreshTokenRequest;
import static org.folio.login.support.TestValues.updateCredentials;
import static org.folio.login.support.TestValues.updatePassword;
import static org.folio.login.support.TestValues.userCredentials;
import static org.folio.test.security.TestJwtGenerator.generateJwtToken;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import feign.FeignException;
import feign.RetryableException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.ws.rs.core.Form;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.folio.login.domain.dto.CredentialsExistence;
import org.folio.login.domain.model.KeycloakUser;
import org.folio.login.domain.model.UserCredentials;
import org.folio.login.exception.RequestValidationException;
import org.folio.login.exception.ServiceException;
import org.folio.login.exception.UnauthorizedException;
import org.folio.login.integration.kafka.LogoutEventPublisher;
import org.folio.login.integration.keycloak.KeycloakClient;
import org.folio.login.support.TestConstants;
import org.folio.spring.FolioExecutionContext;
import org.folio.test.types.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.OAuth2Constants;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class KeycloakServiceTest {

  public static final String KEYCLOAK_USER_ID = UUID.randomUUID().toString();
  @Mock private KeycloakClient keycloakClient;
  @Mock private AdminTokenService adminTokenService;
  @Mock private KeycloakUserService userService;
  @Mock private FolioExecutionContext folioExecutionContext;
  @Mock private RealmConfigurationProvider realmConfigurationProvider;
  @Mock private LogoutEventPublisher logoutEventPublisher;
  @InjectMocks private KeycloakService keycloakService;

  @Test
  void getUserToken_positive() {
    var requestData = loginRequest(USERNAME, PASSWORD, CLIENT_ID, CLIENT_SECRET);
    var realmConfiguration = keycloakRealmConfiguration();

    when(folioExecutionContext.getTenantId()).thenReturn(TENANT);
    when(realmConfigurationProvider.getRealmConfiguration()).thenReturn(realmConfiguration);
    when(keycloakClient.callTokenEndpoint(TENANT, requestData, null, null))
      .thenReturn(keycloakAuthentication());

    var result = keycloakService.getUserToken(loginCredentials(), null, null);

    assertThat(result).isEqualTo(keycloakAuthentication());
  }

  @Test
  void getTokenByAuthCodeFlow_positive() {
    var requestData = loginRequestAuthCode(AUTH_CODE, CLIENT_ID, CLIENT_SECRET, "localhost");
    var realmConfiguration = keycloakRealmConfiguration();

    when(folioExecutionContext.getTenantId()).thenReturn(TENANT);
    when(realmConfigurationProvider.getRealmConfiguration()).thenReturn(realmConfiguration);
    when(keycloakClient.callTokenEndpoint(TENANT, requestData, null, null))
      .thenReturn(keycloakAuthentication());

    var result = keycloakService.getTokenAuthCodeFlow(AUTH_CODE, "localhost", null, null);

    assertThat(result).isEqualTo(keycloakAuthentication());
  }

  @Test
  void getTokenByAuthCodeFlow_negative_keycloakError() {
    var requestData = loginRequestAuthCode(AUTH_CODE, CLIENT_ID, CLIENT_SECRET, "localhost");
    var realmConfiguration = keycloakRealmConfiguration();

    when(folioExecutionContext.getTenantId()).thenReturn(TENANT);
    when(realmConfigurationProvider.getRealmConfiguration()).thenReturn(realmConfiguration);
    when(keycloakClient.callTokenEndpoint(TENANT, requestData, null, null))
      .thenThrow(RetryableException.class);

    assertThatThrownBy(() ->
      keycloakService.getTokenAuthCodeFlow(AUTH_CODE, "localhost", null, null))
      .isInstanceOf(ServiceException.class)
      .hasMessage("Failed to obtain a token");
  }

  @Test
  void getUserToken_negative_KeycloakError() {
    var requestData = loginRequest(USERNAME, PASSWORD, CLIENT_ID, CLIENT_SECRET);
    var realmConfig = keycloakRealmConfiguration();

    when(folioExecutionContext.getTenantId()).thenReturn(TENANT);
    when(realmConfigurationProvider.getRealmConfiguration()).thenReturn(realmConfig);
    when(keycloakClient.callTokenEndpoint(TENANT, requestData, null, null))
      .thenThrow(RetryableException.class);

    var credentials = loginCredentials();
    assertThatThrownBy(() -> keycloakService.getUserToken(credentials, null, null))
      .isInstanceOf(ServiceException.class)
      .hasMessage("Failed to obtain a token");
  }

  @Test
  void getUserToken_negative_unauthorizedException() {
    var requestData = loginRequest(USERNAME, PASSWORD, CLIENT_ID, CLIENT_SECRET);
    var realmConfig = keycloakRealmConfiguration();

    when(folioExecutionContext.getTenantId()).thenReturn(TENANT);
    when(realmConfigurationProvider.getRealmConfiguration()).thenReturn(realmConfig);
    when(keycloakClient.callTokenEndpoint(TENANT, requestData, null, null))
      .thenThrow(FeignException.Unauthorized.class);

    var credentials = loginCredentials();
    assertThatThrownBy(() -> keycloakService.getUserToken(credentials, null, null))
      .isInstanceOf(UnauthorizedException.class)
      .hasMessage("Unauthorized error");
  }

  @Test
  void updateCredentials_positive() {
    changePassword();
    keycloakService.updateCredentials(null, null, updateCredentials());
    verify(keycloakClient).updateCredentials(REALM, TestConstants.KEYCLOAK_USER_ID, updatePassword(), BEARER_TOKEN);
  }

  void changePassword() {
    when(folioExecutionContext.getTenantId()).thenReturn(TENANT);
    when(adminTokenService.getAdminToken(null, null)).thenReturn(BEARER_TOKEN);
    when(userService.findKeycloakUserIdByUserId(USER_ID, BEARER_TOKEN)).thenReturn(TestConstants.KEYCLOAK_USER_ID);
  }

  @Test
  void resetPassword_positive() {
    var passResetAction = passwordResetAction();
    changePassword();
    keycloakService.resetPassword(passResetAction, USER_ID);
    verify(keycloakClient).updateCredentials(REALM, TestConstants.KEYCLOAK_USER_ID, updatePassword(), BEARER_TOKEN);
  }

  @Test
  void updateCredentials_negative_keycloakError() {
    var updateCredentials = updateCredentials();

    when(adminTokenService.getAdminToken(null, null)).thenThrow(RetryableException.class);

    assertThatThrownBy(() -> keycloakService.updateCredentials(null, null, updateCredentials))
      .isInstanceOf(ServiceException.class)
      .hasMessage("Failed to update credentials for a user: " + USER_ID);
  }

  @Test
  void resetPassword_negative_keycloakError() {
    var passwordResetAction = passwordResetAction();

    when(adminTokenService.getAdminToken(null, null)).thenThrow(RetryableException.class);

    assertThatThrownBy(
      () -> keycloakService.resetPassword(passwordResetAction, USER_ID))
      .isInstanceOf(ServiceException.class)
      .hasMessage("Failed to reset password for a user: " + USER_ID);
  }

  @Test
  void createAuthCredentials_positive() {
    var kcUser = new KeycloakUser();
    kcUser.setId(TestConstants.KEYCLOAK_USER_ID);

    when(folioExecutionContext.getTenantId()).thenReturn(TENANT);
    when(adminTokenService.getAdminToken(null, null)).thenReturn(BEARER_TOKEN);
    when(userService.findKeycloakUserByUsername(USERNAME, BEARER_TOKEN)).thenReturn(kcUser);
    when(keycloakClient.getUserCredentials(REALM, TestConstants.KEYCLOAK_USER_ID, BEARER_TOKEN))
      .thenReturn(emptyList());
    doNothing().when(keycloakClient)
      .updateCredentials(REALM, TestConstants.KEYCLOAK_USER_ID, createPassword(), BEARER_TOKEN);

    keycloakService.createAuthCredentials(loginCredentialsWithoutId());

    verify(keycloakClient).updateCredentials(REALM, TestConstants.KEYCLOAK_USER_ID, createPassword(), BEARER_TOKEN);
  }

  @Test
  void createAuthCredentials_negative_keycloakError() {
    when(adminTokenService.getAdminToken(null, null)).thenThrow(RetryableException.class);

    var credentials = loginCredentials();
    assertThatThrownBy(() -> keycloakService.createAuthCredentials(credentials))
      .isInstanceOf(ServiceException.class)
      .hasMessage("Failed to create auth credentials for a username: " + USERNAME);
  }

  @Test
  void createAuthCredentials_negative_credentialsExist() {
    var kcUser = new KeycloakUser();
    kcUser.setId(TestConstants.KEYCLOAK_USER_ID);

    when(folioExecutionContext.getTenantId()).thenReturn(TENANT);
    when(userService.findKeycloakUserIdByUserId(USER_ID, BEARER_TOKEN)).thenReturn(TestConstants.KEYCLOAK_USER_ID);
    when(adminTokenService.getAdminToken(null, null)).thenReturn(BEARER_TOKEN);
    when(keycloakClient.getUserCredentials(REALM, TestConstants.KEYCLOAK_USER_ID, BEARER_TOKEN)).thenReturn(
      List.of(userCredentials()));

    var credentials = loginCredentials();
    assertThatThrownBy(() -> keycloakService.createAuthCredentials(credentials))
      .isInstanceOf(RequestValidationException.class)
      .hasMessage("There already exists credentials for a user: " + USERNAME);
  }

  @Test
  void deleteCredentials_positive() {
    when(folioExecutionContext.getTenantId()).thenReturn(TENANT);
    when(adminTokenService.getAdminToken(null, null)).thenReturn(BEARER_TOKEN);
    when(userService.findKeycloakUserIdByUserId(USER_ID, BEARER_TOKEN)).thenReturn(TestConstants.KEYCLOAK_USER_ID);
    when(keycloakClient.getUserCredentials(REALM, TestConstants.KEYCLOAK_USER_ID, BEARER_TOKEN)).thenReturn(
      List.of(userCredentials()));

    keycloakService.deleteAuthCredentials(USER_ID);

    verify(keycloakClient).deleteUsersCredentials(REALM, TestConstants.KEYCLOAK_USER_ID, USER_CREDENTIAL_ID,
      BEARER_TOKEN);
  }

  @Test
  void deleteCredentials_keycloakError() {
    when(folioExecutionContext.getTenantId()).thenReturn(TENANT);
    when(adminTokenService.getAdminToken(null, null)).thenThrow(RetryableException.class);

    assertThatThrownBy(() -> keycloakService.deleteAuthCredentials(USER_ID))
      .isInstanceOf(ServiceException.class)
      .hasMessage("Failed to delete credentials for a user: " + USER_ID);
  }

  @Test
  void deleteCredentials_credentialsNotFound() {
    when(folioExecutionContext.getTenantId()).thenReturn(TENANT);
    when(adminTokenService.getAdminToken(null, null)).thenReturn(BEARER_TOKEN);
    when(userService.findKeycloakUserIdByUserId(USER_ID, BEARER_TOKEN)).thenReturn(TestConstants.KEYCLOAK_USER_ID);
    when(keycloakClient.getUserCredentials(REALM, TestConstants.KEYCLOAK_USER_ID, BEARER_TOKEN)).thenReturn(
      emptyList());

    assertThatThrownBy(() -> keycloakService.deleteAuthCredentials(USER_ID))
      .isInstanceOf(EntityNotFoundException.class)
      .hasMessage("No credentials for userId " + USER_ID + " found");
  }

  @Test
  void checkCredentialsExistence_positive() {
    var result = checkCredentialsExistence(List.of(userCredentials()));
    assertTrue(result.getCredentialsExist());
  }

  @Test
  void checkCredentialsExistence_positive_returnFalse() {
    var result = checkCredentialsExistence(emptyList());
    assertFalse(result.getCredentialsExist());
  }

  private CredentialsExistence checkCredentialsExistence(List<UserCredentials> list) {
    when(folioExecutionContext.getTenantId()).thenReturn(TENANT);
    when(adminTokenService.getAdminToken(null, null)).thenReturn(BEARER_TOKEN);
    when(userService.findKeycloakUserIdByUserId(USER_ID, BEARER_TOKEN)).thenReturn(TestConstants.KEYCLOAK_USER_ID);

    when(keycloakClient.getUserCredentials(REALM, TestConstants.KEYCLOAK_USER_ID, BEARER_TOKEN)).thenReturn(list);

    return keycloakService.checkCredentialExistence(USER_ID);
  }

  @Test
  void checkCredentialsExistence_negative_keycloakError() {
    when(folioExecutionContext.getTenantId()).thenReturn(TENANT);
    when(adminTokenService.getAdminToken(null, null)).thenThrow(RetryableException.class);

    assertThatThrownBy(() -> keycloakService.checkCredentialExistence(USER_ID))
      .isInstanceOf(ServiceException.class)
      .hasMessage("Failed to get credentials for a user: " + USER_ID);
  }

  @Test
  void logout_positive() {
    var realmConfiguration = keycloakRealmConfiguration();

    when(folioExecutionContext.getTenantId()).thenReturn(TENANT);
    when(realmConfigurationProvider.getRealmConfiguration()).thenReturn(realmConfiguration);

    keycloakService.logout(REFRESH_TOKEN);

    var tokenRequestCaptor = ArgumentCaptor.forClass(Map.class);
    verify(keycloakClient).logout(eq(TENANT), tokenRequestCaptor.capture());

    var expectedForm = new Form().param(OAuth2Constants.REFRESH_TOKEN, REFRESH_TOKEN);
    expectedForm.param(OAuth2Constants.CLIENT_ID, CLIENT_ID);
    expectedForm.param(OAuth2Constants.CLIENT_SECRET, CLIENT_SECRET);

    var actual = tokenRequestCaptor.getValue();
    assertThat(actual).isEqualTo(expectedForm.asMap());
    verify(logoutEventPublisher).publishLogoutEvent(REFRESH_TOKEN);
  }

  @Test
  void logoutAll_positive() {
    when(folioExecutionContext.getTenantId()).thenReturn(TENANT);
    when(folioExecutionContext.getUserId()).thenReturn(USER_UUID);
    when(adminTokenService.getAdminToken(any(), any())).thenReturn(ACCESS_TOKEN);
    when(userService.findKeycloakUserIdByUserId(USER_ID, ACCESS_TOKEN)).thenReturn(KEYCLOAK_USER_ID);

    keycloakService.logoutAll();

    verify(keycloakClient).logoutAll(TENANT, KEYCLOAK_USER_ID, ACCESS_TOKEN);
    verify(logoutEventPublisher).publishLogoutAllEvent(KEYCLOAK_USER_ID);
  }

  @Test
  void refreshToken_positive() {
    var refreshToken = generateJwtToken("http://localhost:8081", TENANT);
    var requestData = refreshTokenRequest(refreshToken);
    var realmConfiguration = keycloakRealmConfiguration();
    var keycloakAuth = keycloakAuthentication();

    when(folioExecutionContext.getTenantId()).thenReturn(TENANT);
    when(realmConfigurationProvider.getRealmConfiguration()).thenReturn(realmConfiguration);
    when(keycloakClient.callTokenEndpoint(any(), any(), any(), any()))
      .thenReturn(keycloakAuth);

    var actualAuth = keycloakService.refreshToken(refreshToken);
    assertThat(actualAuth).isEqualTo(keycloakAuth);

    var captor = ArgumentCaptor.forClass(Map.class);
    verify(keycloakClient).callTokenEndpoint(eq(TENANT), captor.capture(), any(), any());

    var actualTokenRequestPayload = captor.getValue();
    assertThat(actualTokenRequestPayload).isEqualTo(requestData);
  }
}
