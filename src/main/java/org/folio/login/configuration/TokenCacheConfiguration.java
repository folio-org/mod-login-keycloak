package org.folio.login.configuration;

import com.github.benmanes.caffeine.cache.Cache;
import org.folio.login.configuration.property.TokenCacheProperties;
import org.folio.login.domain.model.KeycloakAuthentication;
import org.folio.login.service.AdminTokenCacheFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TokenCacheConfiguration {

  @Bean("adminTokenCache")
  public Cache<String, KeycloakAuthentication> adminTokenCache(TokenCacheProperties tokenCacheProperties) {
    var tokenCacheFactory = new AdminTokenCacheFactory(tokenCacheProperties);
    return tokenCacheFactory.createCache();
  }
}
