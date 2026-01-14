package org.folio.login.service;

import com.github.benmanes.caffeine.cache.Expiry;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.login.domain.model.KeycloakAuthentication;

@Log4j2
@RequiredArgsConstructor
public class TokenCacheExpiry implements Expiry<String, KeycloakAuthentication> {

  private final Function<KeycloakAuthentication, Long> expireAfterCreateFunc;

  @Override
  public long expireAfterCreate(String key, KeycloakAuthentication token, long currentTime) {
    log.debug("expireAfterCreate called: cacheKey = {}, tokenExpiresIn = {}", key, token.getExpiresIn());
    Long expiresAfter = expireAfterCreateFunc.apply(token);
    log.debug("expireAfterCreate result: expiresAfter = {} nanos", expiresAfter);
    return expiresAfter;
  }

  @Override
  public long expireAfterUpdate(String key, KeycloakAuthentication token, long currentTime, long currentDuration) {
    log.debug("expireAfterUpdate called: cacheKey = {}, tokenExpiresIn = {}", key, token.getExpiresIn());
    log.debug("expireAfterUpdate result: expiresAfter = {} nanos", currentDuration);
    return currentDuration;
  }

  @Override
  public long expireAfterRead(String key, KeycloakAuthentication token, long currentTime, long currentDuration) {
    log.debug("expireAfterRead called: cacheKey = {}, tokenExpiresIn = {}", key, token.getExpiresIn());
    log.debug("expireAfterRead result: expiresAfter = {} nanos", currentDuration);
    return currentDuration;
  }
}
