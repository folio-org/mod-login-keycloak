package org.folio.login.integration.users;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "users-keycloak")
public interface UsersKeycloakClient {

  /**
   * Create keycloak user if not exists.
   *
   * @param userId folio user ID, UUID.
   */
  @PostMapping("/auth-users/{userId}")
  void createAuthUserInfo(@PathVariable("userId") String userId);
}
