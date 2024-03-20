package org.folio.login.service;

import static java.time.Instant.ofEpochSecond;
import static org.springframework.http.HttpHeaders.SET_COOKIE;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.login.configuration.property.CookieProperties;
import org.folio.login.domain.model.Token;
import org.folio.login.domain.model.TokenContainer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class TokenCookieHeaderManager {

  public static final String FOLIO_ACCESS_TOKEN = "folioAccessToken";
  public static final String FOLIO_REFRESH_TOKEN = "folioRefreshToken";
  private static final long EXPIRED_DATE_IN_SECONDS = ofEpochSecond(0).toEpochMilli() * 1000;

  private final CookieProperties cookieProperties;

  public HttpHeaders createAuthorizationCookieHeader(TokenContainer tokenContainer) {
    var accessToken = tokenContainer.getAccessToken();
    var refreshToken = tokenContainer.getRefreshToken();

    var headers = new HttpHeaders();
    headers.add(SET_COOKIE, createHeader(FOLIO_ACCESS_TOKEN, accessToken, "/"));
    headers.add(SET_COOKIE, createHeader(FOLIO_REFRESH_TOKEN, refreshToken, "/authn"));
    return headers;
  }

  private String createHeader(String name, Token token, String path) {
    return ResponseCookie.from(name, getOrEmptyString(token))
      .httpOnly(true)
      .secure(true)
      .path(path)
      .maxAge(getOrExpiredDate(token))
      .sameSite(cookieProperties.getSameSiteValue())
      .build()
      .toString();
  }

  private static String getOrEmptyString(Token token) {
    return token != null ? token.getJwt() : "";
  }

  private static Long getOrExpiredDate(Token token) {
    return token != null ? token.getExpiresIn() : EXPIRED_DATE_IN_SECONDS;
  }
}
