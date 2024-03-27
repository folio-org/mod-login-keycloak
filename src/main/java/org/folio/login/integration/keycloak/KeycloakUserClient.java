package org.folio.login.integration.keycloak;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.List;
import org.folio.login.domain.model.KeycloakUser;
import org.folio.login.integration.keycloak.config.KeycloakFeignConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "keycloak-user-client",
  url = "${application.keycloak.url}",
  configuration = KeycloakFeignConfiguration.class)
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
  @GetMapping(value = "/admin/realms/{realm}/users?q={attrQuery}&briefRepresentation={brief}",
    produces = APPLICATION_JSON_VALUE)
  List<KeycloakUser> findUsersWithAttrs(@RequestHeader(AUTHORIZATION) String token,
                                        @PathVariable("realm") String realmName,
                                        @PathVariable("attrQuery") String attrQuery,
                                        @PathVariable("brief") boolean briefRepresentation);

  @GetMapping(value = "/admin/realms/{realm}/users",
    produces = APPLICATION_JSON_VALUE)
  List<KeycloakUser> findUsers(@RequestHeader(AUTHORIZATION) String token,
                               @PathVariable("realm") String realmName,
                               @RequestParam("search") String searchTerm);
}
