package org.folio.login.service;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.login.domain.dto.PasswordCreateAction;
import org.folio.login.domain.dto.PasswordResetAction;
import org.folio.login.domain.dto.ResponseCreateAction;
import org.folio.login.domain.dto.ResponseResetAction;
import org.folio.login.domain.repository.PasswordCreateActionRepository;
import org.folio.login.mapper.PasswordCreateActionMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
@Transactional
@RequiredArgsConstructor
public class PasswordService {

  private final KeycloakService keycloakService;
  private final PasswordCreateActionMapper passwordCreateActionMapper;
  private final PasswordCreateActionRepository passwordCreateActionRepository;

  public ResponseCreateAction createResetPasswordAction(PasswordCreateAction passwordCreateAction) {
    var userId = passwordCreateAction.getUserId();
    var entity = passwordCreateActionMapper.toEntity(passwordCreateAction);
    var isPasswordExist = keycloakService.checkCredentialExistence(userId);

    passwordCreateActionRepository.findById(entity.getId()).ifPresent(actionEntity -> {
      throw new EntityExistsException("Password action with ID: " + actionEntity.getId()
        + " already exist for a user: " + actionEntity.getUserId());
    });
    var response = new ResponseCreateAction();
    response.setPasswordExists(isPasswordExist.getCredentialsExist());

    passwordCreateActionRepository
      .findPasswordCreateActionEntityByUserId(UUID.fromString(userId))
      .ifPresent(passwordCreateActionRepository::delete);
    passwordCreateActionRepository.save(entity);
    return response;
  }

  @Transactional(readOnly = true)
  public PasswordCreateAction getPasswordCreateActionById(String actionId) {
    return passwordCreateActionRepository.findById(UUID.fromString(actionId))
      .map(passwordCreateActionMapper::toDto)
      .orElseThrow(() -> new EntityNotFoundException("Password action with ID: " + actionId
        + " was not found in the db"));
  }

  public ResponseResetAction resetAction(PasswordResetAction passwordResetAction) {
    var action = getPasswordCreateActionById(passwordResetAction.getPasswordResetActionId());

    var isPasswordExist = keycloakService.checkCredentialExistence(action.getUserId());
    var response = new ResponseResetAction();
    response.setIsNewPassword(!isPasswordExist.getCredentialsExist());
    keycloakService.resetPassword(passwordResetAction, action.getUserId());
    passwordCreateActionRepository.deleteById(UUID.fromString(action.getId()));
    return response;
  }
}
