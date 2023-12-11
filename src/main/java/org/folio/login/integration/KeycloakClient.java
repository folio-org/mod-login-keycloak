package org.folio.login.integration;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.USER_AGENT;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import feign.Headers;
import java.util.List;
import java.util.Map;
import org.folio.login.configuration.KeycloakFeignConfiguration;
import org.folio.login.domain.model.KeycloakAuthentication;
import org.folio.login.domain.model.PasswordCredential;
import org.folio.login.domain.model.UserCredentials;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
  name = "keycloak-client",
  url = "${application.keycloak.url}",
  configuration = KeycloakFeignConfiguration.class
)
public interface KeycloakClient {

  /**
   * Retrieves access and refresh tokens for authorization request.
   *
   * @param realm - tenant realm name
   * @param formData - authentication request body
   * @return access and refresh tokens as {@link KeycloakAuthentication} object
   */
  @PostMapping(value = "/realms/{realm}/protocol/openid-connect/token", consumes = APPLICATION_FORM_URLENCODED_VALUE)
  @Headers("Content-Type: application/x-www-form-urlencoded")
  KeycloakAuthentication callTokenEndpoint(
    @PathVariable("realm") String realm,
    @RequestBody Map<String, ?> formData,
    @RequestHeader(USER_AGENT) String userAgent,
    @RequestHeader("X-Forwarded-For") String forwardedFor);

  @PostMapping(value = "/realms/{realm}/protocol/openid-connect/logout", consumes = APPLICATION_FORM_URLENCODED_VALUE)
  void logout(@PathVariable("realm") String realm, @RequestBody Map<String, ?> formData);

  @PostMapping(value = "/admin/realms/{realm}/users/{userId}/logout", consumes = APPLICATION_JSON_VALUE)
  void logoutAll(@PathVariable("realm") String realm, @PathVariable("userId") String userId,
    @RequestHeader(AUTHORIZATION) String token);

  /**
   * Updates user's password.
   *
   * @param realm - tenant realm name
   * @param userId - user's id in keycloak
   * @param passwordCredential - update credentials request body
   * @param token - bearer token
   *
   * */
  @PutMapping(value = "/admin/realms/{realm}/users/{userId}/reset-password")
  void updateCredentials(@PathVariable("realm") String realm,
                         @PathVariable("userId") String userId,
                         @RequestBody PasswordCredential passwordCredential,
                         @RequestHeader(AUTHORIZATION) String token);

  /**
   * Get user's credentials.
   *
   * @param realm - tenant realm name
   * @param userId - user's id in keycloak
   * @param token - bearer token
   *
   * */
  @GetMapping(value = "admin/realms/{realm}/users/{userId}/credentials")
  List<UserCredentials> getUserCredentials(@PathVariable("realm") String realm,
                                          @PathVariable("userId") String userId,
                                          @RequestHeader(AUTHORIZATION) String token);

  /**
   * Delete user's credentials.
   *
   * @param realm - tenant realm name
   * @param userId - user's id in keycloak
   * @param credId - user's credential id in keycloak
   * @param token - bearer token
   *
   * */
  @DeleteMapping(value = "admin/realms/{realm}/users/{userId}/credentials/{credId}")
  void deleteUsersCredentials(@PathVariable("realm") String realm,
                              @PathVariable("userId") String userId,
                              @PathVariable("credId") String credId,
                              @RequestHeader(AUTHORIZATION) String token);
}
