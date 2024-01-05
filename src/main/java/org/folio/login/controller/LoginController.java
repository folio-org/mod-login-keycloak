package org.folio.login.controller;

import static org.apache.commons.lang3.BooleanUtils.isTrue;
import static org.folio.spring.integration.XOkapiHeaders.TOKEN;
import static org.springframework.http.HttpStatus.CREATED;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.login.configuration.property.TokenHeaderProperties;
import org.folio.login.domain.dto.LoginCredentials;
import org.folio.login.domain.dto.LoginResponse;
import org.folio.login.domain.dto.LoginResponseWithExpiry;
import org.folio.login.domain.model.TokenContainer;
import org.folio.login.rest.resource.LoginApi;
import org.folio.login.service.LoginService;
import org.folio.login.service.TokenCookieHeaderManager;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequiredArgsConstructor
public class LoginController implements LoginApi {

  private final LoginService loginService;
  private final TokenHeaderProperties tokenHeaderProperties;
  private final TokenCookieHeaderManager tokenCookieHeaderManager;

  @Override
  public ResponseEntity<LoginResponse> login(LoginCredentials credentials, String userAgent, String forwardedFor) {
    var tokenContainer = loginService.login(credentials, userAgent, forwardedFor);
    var headers = tokenCookieHeaderManager.createAuthorizationCookieHeader(tokenContainer);
    var loginResponse = buildLoginResponse(tokenContainer);
    return ResponseEntity.status(CREATED)
      .headers(headers)
      .headers(createTokenHeaderIfNeeds(tokenContainer.getAccessToken().getJwt()))
      .body(loginResponse);
  }

  @Override
  public ResponseEntity<LoginResponseWithExpiry> loginWithExpiry(LoginCredentials credentials, String userAgent,
    String forwardedFor) {
    var tokenContainer = loginService.login(credentials, userAgent, forwardedFor);
    var headers = tokenCookieHeaderManager.createAuthorizationCookieHeader(tokenContainer);
    var responseBody = buildLoginWithExpiryResponse(tokenContainer);
    return ResponseEntity.status(CREATED)
      .headers(headers)
      .body(responseBody);
  }

  @Override
  public ResponseEntity<Void> logout(String folioRefreshTokenRequired) {
    loginService.logout(folioRefreshTokenRequired);
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<Void> logoutAll() {
    loginService.logoutAll();
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<LoginResponseWithExpiry> token(String code, String redirectUri, String userAgent,
    String forwardedFor) {
    var tokenContainer = loginService.token(code, redirectUri, userAgent, forwardedFor);
    var cookieHeaders = tokenCookieHeaderManager.createAuthorizationCookieHeader(tokenContainer);
    var loginResponse = buildLoginWithExpiryResponse(tokenContainer);
    return ResponseEntity.status(CREATED)
      .headers(cookieHeaders)
      .headers(createTokenHeaderIfNeeds(tokenContainer.getAccessToken().getJwt()))
      .body(loginResponse);
  }

  @Override
  public ResponseEntity<LoginResponseWithExpiry> refreshToken(String folioRefreshToken) {
    var tokenContainer = loginService.refreshToken(folioRefreshToken);
    var headers = tokenCookieHeaderManager.createAuthorizationCookieHeader(tokenContainer);
    var responseBody = buildLoginWithExpiryResponse(tokenContainer);
    return ResponseEntity.status(CREATED)
      .headers(headers)
      .body(responseBody);
  }

  private HttpHeaders createTokenHeaderIfNeeds(String token) {
    var headers = new HttpHeaders();
    if (isTrue(tokenHeaderProperties.getEnabled())) {
      headers.add(TOKEN, token);
    }
    return headers;
  }

  private static LoginResponse buildLoginResponse(TokenContainer tokenContainer) {
    return new LoginResponse().okapiToken(tokenContainer.getAccessToken().getJwt())
      .refreshToken(tokenContainer.getRefreshToken().getJwt());
  }

  private static LoginResponseWithExpiry buildLoginWithExpiryResponse(TokenContainer tokenContainer) {
    return new LoginResponseWithExpiry()
      .accessTokenExpiration(tokenContainer.getAccessTokenExpirationDate())
      .refreshTokenExpiration(tokenContainer.getRefreshTokenExpirationDate());
  }
}
