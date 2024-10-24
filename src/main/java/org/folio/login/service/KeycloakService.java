package org.folio.login.service;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.folio.login.util.JwtUtils.extractTenant;
import static org.folio.login.util.TokenRequestHelper.prepareCodeRequestBody;
import static org.folio.login.util.TokenRequestHelper.prepareRefreshRequestBody;
import static org.keycloak.OAuth2Constants.CLIENT_ID;
import static org.keycloak.OAuth2Constants.CLIENT_SECRET;
import static org.keycloak.OAuth2Constants.REFRESH_TOKEN;

import feign.FeignException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.Form;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.common.utils.OkapiHeaders;
import org.folio.login.domain.dto.CredentialsExistence;
import org.folio.login.domain.dto.LoginCredentials;
import org.folio.login.domain.dto.PasswordResetAction;
import org.folio.login.domain.dto.UpdateCredentials;
import org.folio.login.domain.model.KeycloakAuthentication;
import org.folio.login.domain.model.PasswordCredential;
import org.folio.login.domain.model.UserCredentials;
import org.folio.login.exception.RequestValidationException;
import org.folio.login.exception.ServiceException;
import org.folio.login.exception.UnauthorizedException;
import org.folio.login.integration.kafka.LogoutEventPublisher;
import org.folio.login.integration.keycloak.KeycloakClient;
import org.folio.login.util.TokenRequestHelper;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.scope.FolioExecutionContextSetter;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class KeycloakService {

  private static final String GRANT_TYPE_PASSWORD = "password";
  private final AdminTokenService adminTokenService;
  private final KeycloakUserService keycloakUserService;
  private final KeycloakClient keycloakClient;
  private final FolioExecutionContext folioExecutionContext;
  private final RealmConfigurationProvider realmConfigurationProvider;
  private final LogoutEventPublisher logoutEventPublisher;

  public KeycloakAuthentication getUserToken(LoginCredentials credentials, String userAgent, String forwardedFor) {
    var realmConfiguration = realmConfigurationProvider.getRealmConfiguration();
    var requestData = TokenRequestHelper.preparePasswordRequestBody(credentials, realmConfiguration);
    return getToken(userAgent, forwardedFor, requestData);
  }

  public void logout(String refreshToken) {
    var realm = realmConfigurationProvider.getRealmConfiguration();
    var form = new Form().param(REFRESH_TOKEN, refreshToken);
    form.param(CLIENT_ID, realm.getClientId());
    form.param(CLIENT_SECRET, realm.getClientSecret());

    logoutEventPublisher.publishLogoutEvent(refreshToken);
    var tenantId = folioExecutionContext.getTenantId();
    keycloakClient.logout(tenantId, form.asMap());
  }

  public void logoutAll() {
    var userId = folioExecutionContext.getUserId();
    if (userId == null) {
      throw new BadRequestException("User id is not found");
    }
    log.debug("Logging out all user sessions [userId: {}]", userId);
    var tenantId = folioExecutionContext.getTenantId();
    var token = adminTokenService.getAdminToken(null, null);
    var keycloakUserId = keycloakUserService.findKeycloakUserIdByUserId(userId.toString(), token);

    logoutEventPublisher.publishLogoutAllEvent(keycloakUserId);
    keycloakClient.logoutAll(tenantId, keycloakUserId, token);
  }

  /**
   * Refreshes the access token using the refresh token. The original realm is extracted from the refresh token. The
   * `x-okapi-tenant` header is overridden with the tenant from the refresh token.
   *
   * @param refreshToken the refresh token
   * @return the refreshed access token
   */
  public KeycloakAuthentication refreshToken(String refreshToken) {
    var headers = new HashMap<>(folioExecutionContext.getAllHeaders());
    headers.put(OkapiHeaders.TENANT, List.of(extractTenant(refreshToken)));
    try (var ctx = new FolioExecutionContextSetter(folioExecutionContext.getFolioModuleMetadata(), headers)) {
      var realmConfiguration = realmConfigurationProvider.getRealmConfiguration();
      var requestData = prepareRefreshRequestBody(refreshToken, realmConfiguration);
      return getToken(null, null, requestData);
    }
  }

  public KeycloakAuthentication getTokenAuthCodeFlow(String code, String redirectUri, String userAgent,
    String forwardedFor) {
    var realmConfiguration = realmConfigurationProvider.getRealmConfiguration();
    var requestData = prepareCodeRequestBody(code, redirectUri, realmConfiguration);
    return getToken(userAgent, forwardedFor, requestData);
  }

  @SuppressWarnings("unused")
  public void updateCredentials(String userAgent, String forwardedFor, UpdateCredentials updateCredentials) {
    var userId = updateCredentials.getUserId();
    changePassword(userId, updateCredentials.getNewPassword(), "Failed to update credentials for a user: " + userId);
  }

  public void createAuthCredentials(LoginCredentials loginCredentials) {
    var userId = loginCredentials.getUserId();
    var userName = loginCredentials.getUsername();
    try {
      var token = adminTokenService.getAdminToken(null, null);
      var tenantId = folioExecutionContext.getTenantId();
      var keycloakUserId = isNotEmpty(userId)
        ? keycloakUserService.findKeycloakUserIdByUserId(userId, token)
        : keycloakUserService.findKeycloakUserByUsername(userName, token).getId();
      var userCredentials = keycloakClient.getUserCredentials(tenantId, keycloakUserId, token);

      if (!userCredentials.isEmpty()) {
        throw new RequestValidationException("There already exists credentials for a user: " + userName,
          "username", userName);
      }

      var newPassword = PasswordCredential.of(false, GRANT_TYPE_PASSWORD, loginCredentials.getPassword());
      keycloakClient.updateCredentials(tenantId, keycloakUserId, newPassword, token);
    } catch (FeignException cause) {
      throw new ServiceException("Failed to create auth credentials for a username: " + userName, cause);
    }
  }

  public void deleteAuthCredentials(String userId) {
    var tenantId = folioExecutionContext.getTenantId();
    try {
      var token = adminTokenService.getAdminToken(null, null);
      var keycloakUserId = keycloakUserService.findKeycloakUserIdByUserId(userId, token);
      var userCredentials = keycloakClient.getUserCredentials(tenantId, keycloakUserId, token)
        .stream()
        .filter(credentials -> credentials.getType().equals(GRANT_TYPE_PASSWORD))
        .map(UserCredentials::getId)
        .toList();
      if (userCredentials.isEmpty()) {
        throw new EntityNotFoundException("No credentials for userId " + userId + " found");
      }
      keycloakClient.deleteUsersCredentials(tenantId, keycloakUserId, userCredentials.get(0), token);
    } catch (FeignException cause) {
      throw new ServiceException("Failed to delete credentials for a user: " + userId, cause);
    }
  }

  public CredentialsExistence checkCredentialExistence(String userId) {
    var tenantId = folioExecutionContext.getTenantId();
    try {
      var token = adminTokenService.getAdminToken(null, null);
      var keycloakUserId = keycloakUserService.findKeycloakUserIdByUserId(userId, token);
      var userCredentials = keycloakClient.getUserCredentials(tenantId, keycloakUserId, token);
      return new CredentialsExistence().credentialsExist(!userCredentials.isEmpty());
    } catch (FeignException cause) {
      throw new ServiceException("Failed to get credentials for a user: " + userId, cause);
    }
  }

  public void resetPassword(PasswordResetAction passwordResetAction, String userId) {
    changePassword(userId, passwordResetAction.getNewPassword(), "Failed to reset password for a user: " + userId);
  }

  private void changePassword(String userId, String newPassword, String message) {
    var tenantId = folioExecutionContext.getTenantId();
    try {
      var token = adminTokenService.getAdminToken(null, null);
      var keycloakUserId = keycloakUserService.findKeycloakUserIdByUserId(userId, token);
      var kcPasswordReset = PasswordCredential.of(false, GRANT_TYPE_PASSWORD,
        newPassword);
      keycloakClient.updateCredentials(tenantId, keycloakUserId, kcPasswordReset, token);
    } catch (FeignException cause) {
      throw new ServiceException(message, cause);
    }
  }

  private KeycloakAuthentication getToken(String userAgent, String forwardedFor, Map<String, String> payload) {
    var tenantId = folioExecutionContext.getTenantId();
    try {
      return keycloakClient.callTokenEndpoint(tenantId, payload, userAgent, forwardedFor);
    } catch (FeignException.Unauthorized e) {
      throw new UnauthorizedException("Unauthorized error", e);
    } catch (FeignException cause) {
      throw new ServiceException("Failed to obtain a token", cause);
    }
  }
}
