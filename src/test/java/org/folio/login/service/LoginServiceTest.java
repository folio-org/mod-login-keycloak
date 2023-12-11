package org.folio.login.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.folio.login.support.TestConstants.ACCESS_TOKEN;
import static org.folio.login.support.TestConstants.ACCESS_TOKEN_EXPIRATION_DATE;
import static org.folio.login.support.TestConstants.AUTH_CODE;
import static org.folio.login.support.TestConstants.REFRESH_TOKEN;
import static org.folio.login.support.TestConstants.REFRESH_TOKEN_EXPIRATION_DATE;
import static org.folio.login.support.TestValues.keycloakAuthentication;
import static org.folio.login.support.TestValues.loginCredentials;
import static org.folio.login.support.TestValues.tokenContainer;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.folio.login.exception.TokenLogoutException;
import org.folio.login.exception.TokenRefreshException;
import org.folio.test.types.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

  @Mock private KeycloakService keycloakService;
  @Mock private JwtTokenParser tokenParser;

  @InjectMocks private LoginService loginService;

  @Test
  void login_positive() {
    var auth = keycloakAuthentication();
    var tokenContainer = tokenContainer();

    prepareTokenParserMocks();
    when(keycloakService.getUserToken(loginCredentials(), null, null))
      .thenReturn(auth);

    var actual = loginService.login(loginCredentials(), null, null);

    assertThat(actual).isEqualTo(tokenContainer);
  }

  @Test
  void token_positive() {
    var auth = keycloakAuthentication();
    var tokenContainer = tokenContainer();

    prepareTokenParserMocks();
    when(keycloakService.getTokenAuthCodeFlow(AUTH_CODE, "localhost", null, null))
      .thenReturn(auth);

    var actual = loginService.token(AUTH_CODE, "localhost", null, null);

    assertThat(actual).isEqualTo(tokenContainer);
  }

  @Test
  void logout_positive() {
    loginService.logout(REFRESH_TOKEN);
    verify(keycloakService).logout(REFRESH_TOKEN);
  }

  @Test
  void logout_positive_noCookieHeader() {
    loginService.logout(null);
    verifyNoInteractions(tokenParser);
    verify(keycloakService).logoutAll();
  }

  @Test
  void logout_negative_kcError() {
    doThrow(new RuntimeException("Failure")).when(keycloakService).logout(any());

    assertThatThrownBy(() -> loginService.logout(REFRESH_TOKEN))
      .isInstanceOf(TokenLogoutException.class)
      .hasMessage("Authorization server unable to process token logout request");
  }

  @Test
  void logoutAll_positive() {
    loginService.logoutAll();
    verify(keycloakService).logoutAll();
  }

  @Test
  void logoutAll_negative_kcError() {
    doThrow(new RuntimeException("Failure")).when(keycloakService).logoutAll();

    assertThatThrownBy(() -> loginService.logoutAll())
      .isInstanceOf(TokenLogoutException.class)
      .hasMessage("Authorization server unable to process token logout request");
  }

  @Test
  void refreshToken_positive() {
    prepareTokenParserMocks();
    when(keycloakService.refreshToken(REFRESH_TOKEN)).thenReturn(keycloakAuthentication());

    var response = loginService.refreshToken(REFRESH_TOKEN);

    var expected = tokenContainer();
    assertThat(response).isEqualTo(expected);

    verify(keycloakService).refreshToken(REFRESH_TOKEN);
  }

  @Test
  void refreshToken_negative_kcError() {
    when(keycloakService.refreshToken(any())).thenThrow(new RuntimeException("KC error"));

    assertThatThrownBy(() -> loginService.refreshToken("invalid"))
      .isInstanceOf(TokenRefreshException.class)
      .hasMessage("Authorization server unable to process token refresh request");
  }

  private void prepareTokenParserMocks() {
    when(tokenParser.parseExpirationDate(ACCESS_TOKEN)).thenReturn(ACCESS_TOKEN_EXPIRATION_DATE);
    when(tokenParser.parseExpirationDate(REFRESH_TOKEN)).thenReturn(REFRESH_TOKEN_EXPIRATION_DATE);
  }
}
