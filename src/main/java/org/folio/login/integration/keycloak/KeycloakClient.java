package org.folio.login.integration.keycloak;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.USER_AGENT;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.List;
import org.folio.login.domain.model.KeycloakAuthentication;
import org.folio.login.domain.model.PasswordCredential;
import org.folio.login.domain.model.UserCredentials;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

@HttpExchange(url = "${application.keycloak.url}")
public interface KeycloakClient {

  /**
   * Retrieves access and refresh tokens for authorization request.
   *
   * @param realm - tenant realm name
   * @param formData - authentication request body
   * @return access and refresh tokens as {@link KeycloakAuthentication} object
   */
  @PostExchange(value = "/realms/{realm}/protocol/openid-connect/token",
    contentType = APPLICATION_FORM_URLENCODED_VALUE)
  KeycloakAuthentication callTokenEndpoint(
    @PathVariable("realm") String realm,
    @RequestBody MultiValueMap<String, String> formData,
    @RequestHeader(value = USER_AGENT, required = false) String userAgent,
    @RequestHeader(value = "X-Forwarded-For", required = false) String forwardedFor);

  @PostExchange(value = "/realms/{realm}/protocol/openid-connect/logout",
    contentType = APPLICATION_FORM_URLENCODED_VALUE)
  void logout(@PathVariable("realm") String realm, @RequestBody MultiValueMap<String, String> formData);

  @PostExchange(value = "/admin/realms/{realm}/users/{userId}/logout",
    contentType = APPLICATION_JSON_VALUE)
  void logoutAll(@PathVariable("realm") String realm, @PathVariable("userId") String userId,
    @RequestHeader(AUTHORIZATION) String token);

  /**
   * Updates user's password.
   *
   * @param realm - tenant realm name
   * @param userId - user's id in keycloak
   * @param passwordCredential - update credentials request body
   * @param token - bearer token
   */
  @PutExchange(value = "/admin/realms/{realm}/users/{userId}/reset-password")
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
   */
  @GetExchange(value = "admin/realms/{realm}/users/{userId}/credentials")
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
   */
  @DeleteExchange(value = "admin/realms/{realm}/users/{userId}/credentials/{credId}")
  void deleteUsersCredentials(@PathVariable("realm") String realm,
    @PathVariable("userId") String userId,
    @PathVariable("credId") String credId,
    @RequestHeader(AUTHORIZATION) String token);
}
