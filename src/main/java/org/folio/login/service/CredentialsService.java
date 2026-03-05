package org.folio.login.service;

import static org.apache.commons.lang3.StringUtils.isEmpty;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.login.domain.dto.CredentialsExistence;
import org.folio.login.domain.dto.LoginCredentials;
import org.folio.login.domain.dto.UpdateCredentials;
import org.folio.login.integration.users.UserService;
import org.folio.login.integration.users.UsersKeycloakClient;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.exception.NotFoundException;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class CredentialsService {

  private final KeycloakService keycloakService;
  private final UsersKeycloakClient usersKeycloakClient;
  private final UserService userService;
  private final FolioExecutionContext folioExecutionContext;

  public void createAuthCredentials(LoginCredentials loginCredentials) {
    usersKeycloakClient.createAuthUserInfo(loginCredentials.getUserId());
    keycloakService.createAuthCredentials(loginCredentials);
    log.info("Password created [actorUserId: {}, targetUserId: {}]",
      folioExecutionContext.getUserId(), loginCredentials.getUserId());
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
    log.info("Password changed [actorUserId: {}, targetUserId: {}]",
      folioExecutionContext.getUserId(), updateCredentials.getUserId());
  }

  public CredentialsExistence checkCredentialsExistence(String userId) {
    try {
      return keycloakService.checkCredentialExistence(userId);
    } catch (NotFoundException e) {
      return new CredentialsExistence(false);
    }
  }
}
