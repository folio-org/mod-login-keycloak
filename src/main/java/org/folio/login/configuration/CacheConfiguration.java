package org.folio.login.configuration;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheConfiguration {

  @Value("${cache.keycloak-configuration.ttl}")
  private Duration keycloakConfigTtl;

  @Value("${cache.keycloak-configuration.max-size:500}")
  private int keycloakConfigMaxSize;

  @Bean
  public CacheManager cacheManager() {
    var cacheManager = new SimpleCacheManager();
    cacheManager.setCaches(List.of(
      buildCaffeineCache("keycloak-configuration", keycloakConfigMaxSize, keycloakConfigTtl)
    ));
    return cacheManager;
  }

  private static CaffeineCache buildCaffeineCache(String name, int maxSize, Duration ttl) {
    var caffeine = Caffeine.newBuilder()
      .maximumSize(maxSize)
      .expireAfterWrite(ttl);
    return new CaffeineCache(name, caffeine.build());
  }
}
