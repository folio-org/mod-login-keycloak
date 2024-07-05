package org.folio.login.support;

import static java.util.Objects.requireNonNull;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.keycloak.util.TokenUtil;
import org.springframework.cache.CacheManager;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TestUtils {

  public static void cleanUpCaches(CacheManager cacheManager) {
    cacheManager.getCacheNames().forEach(name -> requireNonNull(cacheManager.getCache(name)).clear());
  }

  @SneakyThrows
  public static String extractJwtSessionId(String jwt) {
    var token = TokenUtil.getRefreshToken(jwt);
    return token.getSessionId();
  }
}
