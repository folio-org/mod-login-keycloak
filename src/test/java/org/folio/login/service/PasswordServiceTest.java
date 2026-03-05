package org.folio.login.service;

import static org.folio.login.support.TestConstants.PASSWORD_RESET_ACTION_ID;
import static org.folio.login.support.TestConstants.PASSWORD_RESET_ACTION_UUID;
import static org.folio.login.support.TestConstants.USER_ID;
import static org.folio.login.support.TestConstants.USER_UUID;
import static org.folio.login.support.TestValues.credentialsExistence;
import static org.folio.login.support.TestValues.passwordCreateAction;
import static org.folio.login.support.TestValues.passwordCreateActionEntity;
import static org.folio.login.support.TestValues.passwordResetAction;
import static org.folio.login.support.TestValues.responseResetAction;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import org.folio.login.domain.dto.PasswordCreateAction;
import org.folio.login.domain.dto.ResponseResetAction;
import org.folio.login.domain.entity.PasswordCreateActionEntity;
import org.folio.login.domain.repository.PasswordCreateActionRepository;
import org.folio.login.exception.ServiceException;
import org.folio.login.mapper.PasswordCreateActionMapper;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.exception.NotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PasswordServiceTest {

  @Mock private KeycloakService keycloakService;
  @Mock private PasswordCreateActionMapper passwordCreateActionMapper;
  @Mock private PasswordCreateActionRepository passwordCreateActionRepository;
  @Mock private FolioExecutionContext folioExecutionContext;

  @InjectMocks private PasswordService passwordService;

  private PasswordCreateAction passwordCreateAction;
  private PasswordCreateActionEntity passwordCreateActionEntity;

  void toEntity() {
    passwordCreateAction = passwordCreateAction();
    passwordCreateActionEntity = passwordCreateActionEntity();
    when(passwordCreateActionMapper.toEntity(passwordCreateAction))
      .thenReturn(passwordCreateActionEntity);
  }

  @AfterEach
  void tearDown() {
    verifyNoMoreInteractions(keycloakService, passwordCreateActionMapper, passwordCreateActionRepository,
      folioExecutionContext);
  }

  @Test
  void createResetPasswordAction_positive() {
    toEntity();
    when(keycloakService.checkCredentialExistence(USER_ID))
      .thenReturn(credentialsExistence(true));
    when(passwordCreateActionRepository.findById(PASSWORD_RESET_ACTION_UUID))
      .thenReturn(Optional.empty());

    var response = passwordService.createResetPasswordAction(passwordCreateAction);
    assertTrue(response.getPasswordExists());

    verify(passwordCreateActionRepository).findById(PASSWORD_RESET_ACTION_UUID);
    verify(passwordCreateActionRepository).save(any());
    verify(passwordCreateActionRepository).findPasswordCreateActionEntityByUserId(USER_UUID);
    verify(folioExecutionContext).getUserId();
  }

  @Test
  void createResetPasswordAction_alreadyExists() {
    toEntity();
    when(keycloakService.checkCredentialExistence(USER_ID))
      .thenReturn(credentialsExistence(true));
    when(passwordCreateActionRepository.findById(PASSWORD_RESET_ACTION_UUID))
      .thenReturn(Optional.of(passwordCreateActionEntity));
    assertThrows(EntityExistsException.class,
      () -> passwordService.createResetPasswordAction(passwordCreateAction),
      "Password action with ID: " + PASSWORD_RESET_ACTION_ID
        + " already exist for a user: " + USER_ID);
  }

  @Test
  void createResetPasswordAction_negative_userNotFound() {
    toEntity();
    when(keycloakService.checkCredentialExistence(USER_ID))
      .thenThrow(new NotFoundException("User not found"));

    assertThrows(NotFoundException.class,
      () -> passwordService.createResetPasswordAction(passwordCreateAction),
      "User not found");
  }

  @Test
  void createResetPasswordAction_negative_serviceException() {
    toEntity();
    when(keycloakService.checkCredentialExistence(USER_ID))
      .thenThrow(new ServiceException("Failed to get credentials for a user: " + USER_ID, new RuntimeException()));

    assertThrows(ServiceException.class,
      () -> passwordService.createResetPasswordAction(passwordCreateAction),
      "Failed to get credentials for a user: " + USER_ID);
  }

  @Test
  void getPasswordCreateActionById_positive() {

    when(passwordCreateActionRepository.findById(PASSWORD_RESET_ACTION_UUID))
      .thenReturn(Optional.of(passwordCreateActionEntity()));
    when(passwordCreateActionMapper.toDto(passwordCreateActionEntity())).thenReturn(passwordCreateAction());

    var result = passwordService.getPasswordCreateActionById(PASSWORD_RESET_ACTION_ID);

    assertNotNull(result);
    verify(passwordCreateActionRepository).findById(PASSWORD_RESET_ACTION_UUID);
    verify(passwordCreateActionMapper).toDto(passwordCreateActionEntity());
  }

  @Test
  void getPasswordCreateActionById_notFound() {
    when(passwordCreateActionRepository.findById(PASSWORD_RESET_ACTION_UUID))
      .thenReturn(Optional.empty());

    assertThrows(EntityNotFoundException.class,
      () -> passwordService.getPasswordCreateActionById(PASSWORD_RESET_ACTION_ID),
      "Password action with ID: " + PASSWORD_RESET_ACTION_ID
        + " was not found in the db");

    verify(passwordCreateActionRepository).findById(PASSWORD_RESET_ACTION_UUID);
  }

  @Test
  void resetAction_positive() {
    var response = resetAction(true);
    assertEquals(responseResetAction(false), response);
  }

  @Test
  void resetAction_positive_isPasswordNewTrue() {
    var response = resetAction(false);
    assertEquals(responseResetAction(true), response);
  }

  ResponseResetAction resetAction(boolean credentialExist) {
    var passResetAction = passwordResetAction();
    var passwordCreateActionEntity = passwordCreateActionEntity();

    when(passwordCreateActionRepository.findById(PASSWORD_RESET_ACTION_UUID))
      .thenReturn(Optional.of(passwordCreateActionEntity));
    when(passwordCreateActionMapper.toDto(passwordCreateActionEntity())).thenReturn(passwordCreateAction());

    when(keycloakService.checkCredentialExistence(USER_ID))
      .thenReturn(credentialsExistence(credentialExist));

    doNothing().when(keycloakService).resetPassword(passResetAction, USER_ID);
    doNothing().when(passwordCreateActionRepository).deleteById(PASSWORD_RESET_ACTION_UUID);
    var response = passwordService.resetAction(passResetAction);

    verify(folioExecutionContext).getUserId();
    return response;
  }
}
