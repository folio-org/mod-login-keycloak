package org.folio.login.controller;

import lombok.RequiredArgsConstructor;
import org.folio.login.domain.dto.LoginAttempts;
import org.folio.login.rest.resource.LoginAttemptsApi;
import org.folio.login.service.LoginAttemptsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class LoginAttemptsController implements LoginAttemptsApi {
  private final LoginAttemptsService service;

  @Override
  public ResponseEntity<LoginAttempts> getLoginAttempts(String userId) {
    var result = service.getLoginAttempts(userId);
    return ResponseEntity.ok(result);
  }
}
