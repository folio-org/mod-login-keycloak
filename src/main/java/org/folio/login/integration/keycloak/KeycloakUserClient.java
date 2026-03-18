package org.folio.login.integration.keycloak;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import java.util.List;
import org.folio.login.domain.model.KeycloakUser;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange(url = "${application.keycloak.url}")
public interface KeycloakUserClient {

  /**
   * Finds users by attribute query.
   *
   * @param token - bearer token
   * @param realmName - tenant realm name
   * @param attrQuery - query for finding users by attributes
   * @param briefRepresentation - is brief representation
   * @return a {@link List} with {@link KeycloakUser}
   */
  @GetExchange(value = "/admin/realms/{realm}/users")
  List<KeycloakUser> findUsersWithAttrs(@RequestHeader(AUTHORIZATION) String token,
    @PathVariable("realm") String realmName,
    @RequestParam("q") String attrQuery,
    @RequestParam("briefRepresentation") boolean briefRepresentation);

  @GetExchange(value = "/admin/realms/{realm}/users")
  List<KeycloakUser> findUsers(@RequestHeader(AUTHORIZATION) String token,
    @PathVariable("realm") String realmName,
    @RequestParam("search") String searchTerm);
}
