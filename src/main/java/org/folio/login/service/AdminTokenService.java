package org.folio.login.service;

import lombok.RequiredArgsConstructor;
import org.folio.login.domain.dto.LoginCredentials;
import org.folio.login.domain.model.KeycloakRealmConfiguration;
import org.folio.login.integration.keycloak.KeycloakClient;
import org.folio.login.integration.keycloak.config.KeycloakProperties;
import org.folio.login.util.TokenRequestHelper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminTokenService {

  private static final String BEARER = "Bearer";
  private final KeycloakProperties keycloakProperties;
  private final KeycloakClient keycloakClient;

  @Cacheable(cacheNames = "token", key = "'admin-cli-token'")
  public String getAdminToken(String userAgent, String forwardedFor) {
    var adminProperties = keycloakProperties.getAdmin();
    var realmConfig = new KeycloakRealmConfiguration()
      .clientId(adminProperties.getClientId());

    var credentials = new LoginCredentials()
      .password(adminProperties.getPassword())
      .username(adminProperties.getUsername());

    var requestData = TokenRequestHelper.preparePasswordRequestBody(credentials, realmConfig);
    var realm = adminProperties.getRealm();
    var token = keycloakClient.callTokenEndpoint(realm, requestData, userAgent, forwardedFor);
    return String.format("%s %s", BEARER, token.getAccessToken());
  }
}
