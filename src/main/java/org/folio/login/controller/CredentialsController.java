package org.folio.login.controller;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

import lombok.RequiredArgsConstructor;
import org.folio.login.domain.dto.CredentialsExistence;
import org.folio.login.domain.dto.LoginCredentials;
import org.folio.login.domain.dto.UpdateCredentials;
import org.folio.login.rest.resource.CredentialsApi;
import org.folio.login.service.CredentialsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CredentialsController implements CredentialsApi {

  private final CredentialsService credentialsService;

  @Override
  public ResponseEntity<CredentialsExistence> checkCredentialsExistence(String userId) {
    return ResponseEntity.status(OK).body(credentialsService.checkCredentialsExistence(userId));
  }

  @Override
  public ResponseEntity<Void> createCredentials(LoginCredentials loginCredentials) {
    credentialsService.createAuthCredentials(loginCredentials);
    return ResponseEntity.status(CREATED).build();
  }

  @Override
  public ResponseEntity<Void> deleteCredentials(String userId) {
    credentialsService.deleteAuthCredentials(userId);
    return ResponseEntity.status(NO_CONTENT).build();
  }

  @Override
  public ResponseEntity<Void> updateCredentials(UpdateCredentials updateCredentials, String userAgent,
    String forwardedFor) {
    credentialsService.updateCredentials(updateCredentials, userAgent, forwardedFor);
    return ResponseEntity.status(NO_CONTENT).build();
  }
}
