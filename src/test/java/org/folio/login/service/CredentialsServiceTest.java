package org.folio.login.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.login.support.TestConstants.USER_ID;
import static org.folio.login.support.TestValues.loginCredentials;
import static org.folio.login.support.TestValues.updateCredentials;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.folio.login.domain.dto.CredentialsExistence;
import org.folio.login.integration.users.UsersKeycloakClient;
import org.folio.test.types.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class CredentialsServiceTest {

  @Mock private KeycloakService keycloakService;
  @Mock private UsersKeycloakClient usersKeycloakClient;
  @InjectMocks private CredentialsService credentialsService;

  @Test
  void createCredentials_positive() {
    doNothing().when(usersKeycloakClient).createAuthUserInfo(loginCredentials().getUserId());
    doNothing().when(keycloakService).createAuthCredentials(loginCredentials());
    credentialsService.createAuthCredentials(loginCredentials());
    verify(keycloakService).createAuthCredentials(loginCredentials());
  }

  @Test
  void deleteCredentials_positive() {
    doNothing().when(keycloakService).deleteAuthCredentials(USER_ID);
    credentialsService.deleteAuthCredentials(USER_ID);
    verify(keycloakService).deleteAuthCredentials(USER_ID);
  }

  @Test
  void updateCredentials_positive() {
    var updateCredentials = updateCredentials();
    var userAgent = "user-agent-test";
    var forwardedFor = "forwarded-for-test";

    doNothing().when(usersKeycloakClient).createAuthUserInfo(updateCredentials.getUserId());
    doNothing().when(keycloakService).updateCredentials(userAgent, forwardedFor, updateCredentials);

    credentialsService.updateCredentials(updateCredentials, userAgent, forwardedFor);

    verify(usersKeycloakClient).createAuthUserInfo(updateCredentials.getUserId());
    verify(keycloakService).updateCredentials(userAgent, forwardedFor, updateCredentials);
  }

  @Test
  void checkCredentials_positive() {
    checkCredentials(true);
  }

  @Test
  void checkCredentials_negative() {
    checkCredentials(false);
  }

  void checkCredentials(boolean value) {
    var credentialsExistence = new CredentialsExistence().credentialsExist(value);
    when(keycloakService.checkCredentialExistence(USER_ID)).thenReturn(credentialsExistence);
    var result = credentialsService.checkCredentialsExistence(USER_ID);
    assertThat(result).isEqualTo(credentialsExistence);
  }
}
