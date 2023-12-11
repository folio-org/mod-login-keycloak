package org.folio.login.service;

import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.folio.login.domain.dto.LoginAttempts;
import org.folio.spring.FolioExecutionContext;
import org.keycloak.admin.client.Keycloak;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginAttemptsService {
  private static final String ATTEMPTS_PROP = "numFailures";
  private static final String LAST_ATTEMPT_PROP = "lastFailure";

  private final Keycloak keycloak;
  private final KeycloakUserService userService;
  private final FolioExecutionContext folioExecutionContext;

  public LoginAttempts getLoginAttempts(String folioUserId) {
    var tenant = folioExecutionContext.getTenantId();
    var authHeaderValue = getAuthHeaderValue();
    var keycloakUserId = userService.findKeycloakUserIdByUserId(folioUserId, authHeaderValue);

    var bruteForceUserStatus = keycloak.realm(tenant).attackDetection().bruteForceUserStatus(keycloakUserId);

    var numFailures = Integer.parseInt(String.valueOf(bruteForceUserStatus.get(ATTEMPTS_PROP)));
    var lastFailureTs = Long.parseLong(String.valueOf(bruteForceUserStatus.get(LAST_ATTEMPT_PROP)));

    return new LoginAttempts()
      .userId(folioUserId)
      .attemptCount(numFailures)
      .lastAttempt(new Date(lastFailureTs));
  }

  private String getAuthHeaderValue() {
    var accessToken = keycloak.tokenManager().getAccessToken();
    return String.format("%s %s", accessToken.getTokenType(), accessToken.getToken());
  }
}
