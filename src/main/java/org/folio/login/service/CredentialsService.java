package org.folio.login.service;

import static org.apache.commons.lang3.StringUtils.isEmpty;

import lombok.RequiredArgsConstructor;
import org.folio.login.domain.dto.CredentialsExistence;
import org.folio.login.domain.dto.LoginCredentials;
import org.folio.login.domain.dto.UpdateCredentials;
import org.folio.login.integration.users.UserService;
import org.folio.login.integration.users.UsersKeycloakClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CredentialsService {

  private final KeycloakService keycloakService;
  private final UsersKeycloakClient usersKeycloakClient;
  private final UserService userService;

  public void createAuthCredentials(LoginCredentials loginCredentials) {
    usersKeycloakClient.createAuthUserInfo(loginCredentials.getUserId());
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
    if (isEmpty(updateCredentials.getUserId())) {
      var user = userService.getUserByUsername(updateCredentials.getUsername());
      updateCredentials.setUserId(user.getId().toString());
    }

    usersKeycloakClient.createAuthUserInfo(updateCredentials.getUserId());
    keycloakService.updateCredentials(userAgent, forwardedFor, updateCredentials);
  }

  public CredentialsExistence checkCredentialsExistence(String userId) {
    return keycloakService.checkCredentialExistence(userId);
  }
}
