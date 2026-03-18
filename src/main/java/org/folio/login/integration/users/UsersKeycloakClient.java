package org.folio.login.integration.users;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange(url = "users-keycloak")
public interface UsersKeycloakClient {

  /**
   * Create keycloak user if not exists.
   *
   * @param userId folio user ID, UUID.
   */
  @PostExchange("/auth-users/{userId}")
  void createAuthUserInfo(@PathVariable("userId") String userId);
}
