package org.folio.login.service;

import static org.springframework.http.HttpHeaders.SET_COOKIE;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.login.configuration.property.CookieProperties;
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

  private static final String ACCESS_TOKEN_PATH = "/";
  private static final String FOLIO_REFRESH_PATH = "/authn"; //NOSONAR

  private final CookieProperties cookieProperties;

  public HttpHeaders createAuthorizationCookieHeader(TokenContainer tokenContainer) {
    var accessToken = tokenContainer.getAccessToken();
    var refreshToken = tokenContainer.getRefreshToken();

    var headers = new HttpHeaders();
    headers.add(SET_COOKIE, createHeader(FOLIO_ACCESS_TOKEN, accessToken.getJwt(), accessToken.getExpiresIn(),
      ACCESS_TOKEN_PATH));
    headers.add(SET_COOKIE, createHeader(FOLIO_REFRESH_TOKEN, refreshToken.getJwt(), refreshToken.getExpiresIn(),
      FOLIO_REFRESH_PATH));
    return headers;
  }

  private String createHeader(String name, String value, Long maxAge, String path) {
    return ResponseCookie.from(name, value)
      .httpOnly(true)
      .secure(true)
      .path(path)
      .maxAge(maxAge)
      .sameSite(cookieProperties.getSameSiteValue())
      .build()
      .toString();
  }
}
