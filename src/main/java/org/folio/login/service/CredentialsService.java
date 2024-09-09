package org.folio.login.service;

import lombok.RequiredArgsConstructor;
import org.folio.login.domain.dto.CredentialsExistence;
import org.folio.login.domain.dto.LoginCredentials;
import org.folio.login.domain.dto.UpdateCredentials;
import org.folio.login.integration.users.ModUsersKeycloakClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CredentialsService {

  private final KeycloakService keycloakService;
  private final ModUsersKeycloakClient modUsersKeycloakClient;

  public void createAuthCredentials(LoginCredentials loginCredentials) {
    modUsersKeycloakClient.createAuthUserInfo(loginCredentials.getUserId());
    keycloakService.createAuthCredentials(loginCredentials);
  }

  public void deleteAuthCredentials(String userId) {
    keycloakService.deleteAuthCredentials(userId);
  }

  /**
   * Updates user credentials.
   *
   * @param updateCredentials - update credentials
   * @param userAgent         - user-agent header value
   * @param forwardedFor      - x-forwarded-for header value
   */
  public void updateCredentials(UpdateCredentials updateCredentials, String userAgent, String forwardedFor) {
    keycloakService.updateCredentials(userAgent, forwardedFor, updateCredentials);
  }

  public CredentialsExistence checkCredentialsExistence(String userId) {
    return keycloakService.checkCredentialExistence(userId);
  }
}
