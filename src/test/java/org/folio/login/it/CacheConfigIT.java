package org.folio.login.it;

import static java.time.Duration.ofMillis;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.login.support.TestConstants.KEYCLOAK_CONFIG_CACHE;
import static org.folio.login.support.TestConstants.TOKEN_CACHE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

import org.folio.login.support.base.BaseIntegrationTest;
import org.folio.test.types.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.test.context.TestPropertySource;

@IntegrationTest
@TestPropertySource(properties = {
  "KC_CONFIG_TTL=PT0.1S",
  "KC_ADMIN_TOKEN_TTL=PT0.05S"
})
class CacheConfigIT extends BaseIntegrationTest {

  @Autowired
  private CacheManager cacheManager;

  @BeforeEach
  void setUp() {
    cleanUpCaches(cacheManager);
  }

  @Test
  void testCacheTtl() {
    var keycloakConfigCache = getCaffeineCache(KEYCLOAK_CONFIG_CACHE);
    var tokenCache = getCaffeineCache(TOKEN_CACHE);

    checkCacheTtl(keycloakConfigCache, 100);
    checkCacheTtl(tokenCache, 50);
  }

  @Test
  void testCacheEviction() {
    var keycloakConfigCache = getCaffeineCache(KEYCLOAK_CONFIG_CACHE);
    var tokenCache = getCaffeineCache(TOKEN_CACHE);

    checkCacheEviction(keycloakConfigCache);
    checkCacheEviction(tokenCache);
  }

  @Test
  void testCacheExpiration() {
    var keycloakConfigCache = getCaffeineCache(KEYCLOAK_CONFIG_CACHE);
    var tokenCache = getCaffeineCache(TOKEN_CACHE);

    checkCacheExpiration(keycloakConfigCache, 100);
    checkCacheExpiration(tokenCache, 50);
  }

  CaffeineCache getCaffeineCache(String name) {
    var cache = (CaffeineCache) cacheManager.getCache(name);
    assertNotNull(cache);
    return cache;
  }

  void checkCacheTtl(CaffeineCache cache, long expectedTtl) {
    cache.getNativeCache().policy().expireAfterWrite().ifPresent(ttl ->
      assertThat(ttl.getExpiresAfter().getNano()).isEqualTo(MILLISECONDS.toNanos(expectedTtl)));
  }

  void checkCacheEviction(CaffeineCache cache) {
    cache.put("key1", "value1");
    cache.put("key2", "value2");
    assertEquals(2, cache.getNativeCache().estimatedSize());
    cache.evict("key1");
    assertEquals(1, cache.getNativeCache().estimatedSize());
  }

  void checkCacheExpiration(CaffeineCache cache, long expectedTtl) {
    cache.put("key1", "value1");
    cache.put("key2", "value2");
    assertEquals(2, cache.getNativeCache().estimatedSize());
    await().atMost(ofMillis(expectedTtl + 25)).pollInterval(ofMillis(25)).untilAsserted(() -> {
      assertNull(cache.get("key1"));
      assertNull(cache.get("key2"));
    });
  }

  public static void cleanUpCaches(CacheManager cacheManager) {
    cacheManager.getCacheNames().forEach(name -> requireNonNull(cacheManager.getCache(name)).clear());
  }
}
