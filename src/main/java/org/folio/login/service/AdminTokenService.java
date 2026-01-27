package org.folio.login.service;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.login.domain.dto.LoginCredentials;
import org.folio.login.domain.model.KeycloakAuthentication;
import org.folio.login.domain.model.KeycloakRealmConfiguration;
import org.folio.login.integration.keycloak.KeycloakClient;
import org.folio.login.integration.keycloak.config.KeycloakProperties;
import org.folio.login.util.TokenRequestHelper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class AdminTokenService {

  private static final String BEARER = "Bearer";
  private static final String CACHE_KEY = "admin-cli-token";
  
  private final KeycloakProperties keycloakProperties;
  private final KeycloakClient keycloakClient;
  @Qualifier("adminTokenCache")
  private final Cache<String, KeycloakAuthentication> adminTokenCache;

  public String getAdminToken(String userAgent, String forwardedFor) {
    var token = adminTokenCache.get(CACHE_KEY, key -> {
      log.debug("Cache miss - fetching new admin token from Keycloak");
      var adminProperties = keycloakProperties.getAdmin();
      var realmConfig = new KeycloakRealmConfiguration()
        .clientId(adminProperties.getClientId());

      var credentials = new LoginCredentials()
        .password(adminProperties.getPassword())
        .username(adminProperties.getUsername());

      var requestData = TokenRequestHelper.preparePasswordRequestBody(credentials, realmConfig);
      var realm = adminProperties.getRealm();
      var fetchedToken = keycloakClient.callTokenEndpoint(realm, requestData, userAgent, forwardedFor);

      log.debug("Admin token cached with dynamic TTL based on expiresIn: {} seconds", fetchedToken.getExpiresIn());
      return fetchedToken;
    });

    log.debug("Returning admin token");
    return formatBearerToken(token.getAccessToken());
  }

  private String formatBearerToken(String accessToken) {
    return String.format("%s %s", BEARER, accessToken);
  }
}
