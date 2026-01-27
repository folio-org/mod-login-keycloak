package org.folio.login.service;

import static java.time.Duration.ofSeconds;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import org.folio.login.configuration.property.TokenCacheProperties;
import org.folio.login.domain.model.KeycloakAuthentication;
import org.folio.test.types.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class AdminTokenCacheFactoryTest {

  private static final long EXPIRES_IN_LONG_LIVED = 300L; // 5 minutes
  private static final long EXPIRES_IN_SHORT_LIVED = 40L;
  private static final long EXPIRES_IN_VERY_SHORT = 10L;
  private static final long EXPIRES_IN_BOUNDARY_AT_THRESHOLD = 55L; // With refresh=25, earlyExp=30
  private static final long EXPIRES_IN_BOUNDARY_ABOVE_THRESHOLD = 56L; // With refresh=25, earlyExp=31
  private static final long EXPIRES_IN_ZERO = 0L;

  private static final int REFRESH_BEFORE_EXPIRY_DEFAULT = 25;
  private static final int REFRESH_BEFORE_EXPIRY_NONE = 0;
  private static final int DEFAULT_TTL_SEC = 300;

  private static final long EXPECTED_TTL_LONG_LIVED = 275L; // 300 - 25
  private static final long EXPECTED_TTL_SHORT_LIVED = 40L; // Falls back to full expiresIn
  private static final long EXPECTED_TTL_VERY_SHORT = 10L; // Falls back to full expiresIn
  private static final long EXPECTED_TTL_BOUNDARY_AT = 55L; // Falls back to full expiresIn
  private static final long EXPECTED_TTL_BOUNDARY_ABOVE = 31L; // 56 - 25
  private static final long EXPECTED_TTL_ZERO = 0L;
  private static final long EXPECTED_TTL_NO_EARLY_REFRESH = 300L;

  @Mock private TokenCacheProperties tokenCacheProperties;
  @InjectMocks private AdminTokenCacheFactory factory;

  @Test
  void calculateTtl_positive() {
    var token = createToken(EXPIRES_IN_LONG_LIVED);
    when(tokenCacheProperties.getRefreshBeforeExpirySec()).thenReturn(REFRESH_BEFORE_EXPIRY_DEFAULT);

    long ttlNanos = factory.calculateTtl(token);

    assertEquals(ofSeconds(EXPECTED_TTL_LONG_LIVED).toNanos(), ttlNanos);
  }

  @Test
  void calculateTtl_positive_shortLivedToken() {
    var token = createToken(EXPIRES_IN_SHORT_LIVED);
    when(tokenCacheProperties.getRefreshBeforeExpirySec()).thenReturn(REFRESH_BEFORE_EXPIRY_DEFAULT);

    long ttlNanos = factory.calculateTtl(token);

    assertEquals(ofSeconds(EXPECTED_TTL_SHORT_LIVED).toNanos(), ttlNanos);
  }

  @Test
  void calculateTtl_positive_refreshBeforeExpiryLargeThenExpiresIn() {
    var token = createToken(EXPIRES_IN_VERY_SHORT);
    when(tokenCacheProperties.getRefreshBeforeExpirySec()).thenReturn(REFRESH_BEFORE_EXPIRY_DEFAULT);

    long ttlNanos = factory.calculateTtl(token);

    assertEquals(ofSeconds(EXPECTED_TTL_VERY_SHORT).toNanos(), ttlNanos);
  }

  @Test
  void calculateTtl_positive_earlyExpirationEqualsThreshold() {
    var token = createToken(EXPIRES_IN_BOUNDARY_AT_THRESHOLD);
    when(tokenCacheProperties.getRefreshBeforeExpirySec()).thenReturn(REFRESH_BEFORE_EXPIRY_DEFAULT);

    long ttlNanos = factory.calculateTtl(token);

    assertEquals(ofSeconds(EXPECTED_TTL_BOUNDARY_AT).toNanos(), ttlNanos);
  }

  @Test
  void calculateTtl_positive_earlyExpirationAboveThreshold() {
    var token = createToken(EXPIRES_IN_BOUNDARY_ABOVE_THRESHOLD);
    when(tokenCacheProperties.getRefreshBeforeExpirySec()).thenReturn(REFRESH_BEFORE_EXPIRY_DEFAULT);

    long ttlNanos = factory.calculateTtl(token);

    assertEquals(ofSeconds(EXPECTED_TTL_BOUNDARY_ABOVE).toNanos(), ttlNanos);
  }

  @Test
  void calculateTtl_positive_zeroExpiresIn() {
    var token = createToken(EXPIRES_IN_ZERO);
    when(tokenCacheProperties.getRefreshBeforeExpirySec()).thenReturn(REFRESH_BEFORE_EXPIRY_DEFAULT);

    long ttlNanos = factory.calculateTtl(token);

    assertEquals(ofSeconds(EXPECTED_TTL_ZERO).toNanos(), ttlNanos);
  }

  @Test
  void calculateTtl_positive_noEarlyRefresh() {
    var token = createToken(EXPIRES_IN_LONG_LIVED);
    when(tokenCacheProperties.getRefreshBeforeExpirySec()).thenReturn(REFRESH_BEFORE_EXPIRY_NONE);

    long ttlNanos = factory.calculateTtl(token);

    assertEquals(ofSeconds(EXPECTED_TTL_NO_EARLY_REFRESH).toNanos(), ttlNanos);
  }

  @Test
  void calculateTtl_positive_nullExpiresIn() {
    var token = createToken(null);

    long ttlNanos = factory.calculateTtl(token);

    assertEquals(ofSeconds(DEFAULT_TTL_SEC).toNanos(), ttlNanos);
  }

  @Test
  void createCache_positive() {
    var cache = factory.createCache();

    assertNotNull(cache);
    assertEquals(0, cache.estimatedSize());
  }

  private static KeycloakAuthentication createToken(Long expiresIn) {
    var token = new KeycloakAuthentication();
    token.setExpiresIn(expiresIn);
    return token;
  }
}
