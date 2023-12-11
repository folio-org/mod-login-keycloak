package org.folio.login.service;

import static org.apache.commons.lang3.StringUtils.isBlank;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.login.domain.dto.LoginCredentials;
import org.folio.login.domain.model.KeycloakAuthentication;
import org.folio.login.domain.model.Token;
import org.folio.login.domain.model.TokenContainer;
import org.folio.login.exception.TokenLogoutException;
import org.folio.login.exception.TokenRefreshException;
import org.springframework.stereotype.Service;

@Service
@Log4j2
@RequiredArgsConstructor
public class LoginService {

  private static final String TOKEN_LOGOUT_UNPROCESSABLE =
    "Authorization server unable to process token logout request";
  private static final String TOKEN_REFRESH_UNPROCESSABLE =
    "Authorization server unable to process token refresh request";

  private final KeycloakService keycloakService;
  private final JwtTokenParser tokenParser;

  /**
   * Performs login operation using Keycloak OpenID Token Endpoint.
   *
   * @param credentials  - user credentials
   * @param userAgent    - user-agent header value
   * @param forwardedFor - x-forwarded-for header value
   * @return {@link TokenContainer} object with access and refresh tokens populated with additional information
   */
  public TokenContainer login(LoginCredentials credentials, String userAgent, String forwardedFor) {
    var keycloakAuthentication = keycloakService.getUserToken(credentials, userAgent, forwardedFor);
    return buildTokenContainer(keycloakAuthentication);
  }

  /**
   * Logs the user out on their current device if the refresh token is present in cookie header,
   * otherwise logs out all user sessions.
   *
   * @param refreshToken refresh token (optional).
   * @throws TokenLogoutException   if logout operation fails.
   */
  public void logout(String refreshToken) {
    if (isBlank(refreshToken)) {
      logoutAll();
      return;
    }

    try {
      keycloakService.logout(refreshToken);
    } catch (Exception e) {
      throw new TokenLogoutException(TOKEN_LOGOUT_UNPROCESSABLE, e);
    }
  }

  /**
   * Logs the user out on all of their devices.
   *
   * @throws TokenLogoutException if logout all operation fails.
   */
  public void logoutAll() {
    try {
      keycloakService.logoutAll();
    } catch (Exception e) {
      throw new TokenLogoutException(TOKEN_LOGOUT_UNPROCESSABLE, e);
    }
  }

  /**
   * Exchange temporary authn code for token. This operation use Keycloak OpenID Token Endpoint.
   *
   * @param code         - temporary authentication code to retrieve a real token
   * @param redirectUri  - initial uri that was used as redirect uri for getting authentication code
   * @param userAgent    - user-agent header value
   * @param forwardedFor - x-forwarded-for header value
   * @return {@link TokenContainer} object with access and refresh tokens populated with additional information
   */
  public TokenContainer token(String code, String redirectUri, String userAgent, String forwardedFor) {
    var keycloakAuthentication = keycloakService.getTokenAuthCodeFlow(code, redirectUri, userAgent, forwardedFor);
    return buildTokenContainer(keycloakAuthentication);
  }

  /**
   * Get a new refresh and access token.
   *
   * @param refreshToken refresh token
   * @return {@link TokenContainer} object with access and refresh tokens populated with additional information
   * @throws TokenRefreshException  if token refresh operation fails
   */
  public TokenContainer refreshToken(String refreshToken) {
    try {
      var keycloakAuthentication = keycloakService.refreshToken(refreshToken);
      return buildTokenContainer(keycloakAuthentication);
    } catch (Exception e) {
      throw new TokenRefreshException(TOKEN_REFRESH_UNPROCESSABLE, e);
    }
  }

  private TokenContainer buildTokenContainer(KeycloakAuthentication keycloakAuthentication) {
    var accessToken = Token.builder().jwt(keycloakAuthentication.getAccessToken())
      .expirationDate(tokenParser.parseExpirationDate(keycloakAuthentication.getAccessToken()))
      .expiresIn(keycloakAuthentication.getExpiresIn())
      .build();
    var refreshToken = Token.builder().jwt(keycloakAuthentication.getRefreshToken())
      .expirationDate(tokenParser.parseExpirationDate(keycloakAuthentication.getRefreshToken()))
      .expiresIn(keycloakAuthentication.getRefreshExpiresIn())
      .build();
    return TokenContainer.builder().accessToken(accessToken).refreshToken(refreshToken).build();
  }
}
