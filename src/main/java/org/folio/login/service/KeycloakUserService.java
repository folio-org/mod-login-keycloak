package org.folio.login.service;

import static org.folio.common.utils.CollectionUtils.toStream;
import static org.folio.login.domain.model.KeycloakUser.USER_ID_ATTR;
import static org.springframework.util.CollectionUtils.isEmpty;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.login.domain.model.KeycloakUser;
import org.folio.login.integration.keycloak.KeycloakUserClient;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.exception.NotFoundException;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class KeycloakUserService {

  private final KeycloakUserClient client;
  private final FolioExecutionContext context;

  public String findKeycloakUserIdByUserId(String userId, String adminToken) {
    return findKeycloakUserWithUserIdAttr(userId, adminToken).getId();
  }

  public KeycloakUser findKeycloakUserByUsername(String username, String adminToken) {
    var candidates = client.findUsers(adminToken, context.getTenantId(), username);
    return toStream(candidates)
      .filter(user -> user.getUserName().equals(username))
      .findFirst()
      .orElseThrow(() -> new NotFoundException("Keycloak user doesn't exist with the given 'username': " + username));
  }

  private KeycloakUser findKeycloakUserWithUserIdAttr(String userId, String adminToken) {
    var query = USER_ID_ATTR + ":" + userId;
    var found = client.findUsersWithAttrs(adminToken, context.getTenantId(), query, true);
    if (isEmpty(found)) {
      throw new NotFoundException("Keycloak user doesn't exist with the given 'user_id' attribute: " + userId);
    }
    if (found.size() != 1) {
      throw new IllegalStateException("Too many keycloak users with 'user_id' attribute: " + userId);
    }
    return found.get(0);
  }
}
