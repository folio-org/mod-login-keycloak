package org.folio.login.controller;

import static org.springframework.http.HttpStatus.CREATED;

import lombok.RequiredArgsConstructor;
import org.folio.login.domain.dto.Password;
import org.folio.login.domain.dto.PasswordCreateAction;
import org.folio.login.domain.dto.PasswordResetAction;
import org.folio.login.domain.dto.ResponseCreateAction;
import org.folio.login.domain.dto.ResponseResetAction;
import org.folio.login.domain.dto.ValidPasswordResponse;
import org.folio.login.rest.resource.PasswordApi;
import org.folio.login.service.PasswordService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PasswordController implements PasswordApi {

  private static final ValidPasswordResponse VALID_PASSWORD_RESPONSE = new ValidPasswordResponse().result("valid");

  private final PasswordService passwordService;

  @Override
  public ResponseEntity<ResponseCreateAction> createResetPasswordAction(PasswordCreateAction passwordCreateAction) {
    return ResponseEntity.status(CREATED)
      .body(passwordService.createResetPasswordAction(passwordCreateAction));
  }

  @Override
  public ResponseEntity<PasswordCreateAction> getPasswordActionById(String actionId) {
    return ResponseEntity.ok(passwordService.getPasswordCreateActionById(actionId));
  }

  @Override
  public ResponseEntity<ResponseResetAction> resetPassword(PasswordResetAction passwordResetAction) {
    return ResponseEntity.status(CREATED)
      .body(passwordService.resetAction(passwordResetAction));
  }

  /**
   * Validate password repeatability.
   * It is a dummy method that always returns "valid" response. Was decided to implement it this way because of the
   * following reasons: Keycloak has its own password validation mechanism.
   *
   * @param password  (required)
   * @return {@link ValidPasswordResponse} is password valid
   */
  @Override
  public ResponseEntity<ValidPasswordResponse> validatePasswordRepeatability(Password password) {
    return ResponseEntity.ok(VALID_PASSWORD_RESPONSE);
  }
}
