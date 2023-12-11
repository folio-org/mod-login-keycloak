package org.folio.login.service;

import lombok.RequiredArgsConstructor;
import org.folio.common.configuration.properties.FolioEnvironment;
import org.folio.login.configuration.KeycloakConfigurationProperties;
import org.folio.login.domain.model.KeycloakRealmConfiguration;
import org.folio.spring.FolioExecutionContext;
import org.folio.tools.store.SecureStore;
import org.folio.tools.store.exception.NotFoundException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RealmConfigurationProvider {

  private final SecureStore secureStore;
  private final FolioEnvironment folioEnvironment;
  private final FolioExecutionContext folioExecutionContext;
  private final KeycloakConfigurationProperties keycloakConfigurationProperties;

  /**
   * Provides realm configuration using {@link FolioExecutionContext} object.
   *
   * @return {@link KeycloakRealmConfiguration} object for user authentication
   */
  @Cacheable(cacheNames = "keycloak-configuration", key = "@folioExecutionContext.tenantId")
  public KeycloakRealmConfiguration getRealmConfiguration() {
    var tenantId = folioExecutionContext.getTenantId();
    var clientId = tenantId + keycloakConfigurationProperties.getClientSuffix();
    return new KeycloakRealmConfiguration()
      .clientId(clientId)
      .clientSecret(retrieveKcClientSecret(tenantId, clientId));
  }

  private String retrieveKcClientSecret(String tenantId, String clientId) {
    try {
      return secureStore.get(buildKey(folioEnvironment.getEnvironment(), tenantId, clientId));
    } catch (NotFoundException e) {
      throw new IllegalStateException(String.format(
        "Failed to get value from secure store [tenantId: %s, clientId: %s]", tenantId, clientId), e);
    }
  }

  private String buildKey(String env, String tenantId, String clientId) {
    return String.format("%s_%s_%s", env, tenantId, clientId);
  }
}
