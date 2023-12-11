package org.folio.login.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.login.support.TestConstants.ACCESS_TOKEN;
import static org.folio.login.support.TestConstants.EXPIRES_IN;
import static org.folio.login.support.TestConstants.REFRESH_EXPIRES_IN;
import static org.folio.login.support.TestConstants.REFRESH_TOKEN;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.SET_COOKIE;

import org.folio.login.configuration.property.CookieProperties;
import org.folio.login.support.TestValues;
import org.folio.test.types.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class TokenCookieManagerTest {

  @InjectMocks private TokenCookieHeaderManager service;
  @Mock private CookieProperties properties;

  @Test
  void createHttpOnlyHeaders_positive() {
    var tokenContainer = TestValues.tokenContainer();

    when(properties.getSameSiteValue()).thenReturn("None");
    var result = service.createAuthorizationCookieHeader(tokenContainer);

    assertThat(result).hasSize(1);
    assertThat(result.get(SET_COOKIE)).hasSize(2);
    assertThat(result.get(SET_COOKIE).get(0).split(" "))
      .contains(String.format("folioAccessToken=%s;", ACCESS_TOKEN), "Path=/;",
        String.format("Max-Age=%s;", EXPIRES_IN),
        "Secure;", "HttpOnly;", String.format("SameSite=%s", properties.getSameSiteValue()));
    assertThat(result.get(SET_COOKIE).get(1).split(" "))
      .contains(String.format("folioRefreshToken=%s;", REFRESH_TOKEN), "Path=/;",
        String.format("Max-Age=%s;", REFRESH_EXPIRES_IN),
        "Secure;", "HttpOnly;", String.format("SameSite=%s", properties.getSameSiteValue()));
  }
}
