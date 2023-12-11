package org.folio.login.controller;

import static org.folio.login.support.TestConstants.PASSWORD_RESET_ACTION_ID;
import static org.folio.login.support.TestConstants.TENANT;
import static org.folio.login.support.TestConstants.USER_ID;
import static org.folio.login.support.TestValues.passwordCreateAction;
import static org.folio.login.support.TestValues.passwordResetAction;
import static org.folio.login.support.TestValues.responseCreateAction;
import static org.folio.login.support.TestValues.responseResetAction;
import static org.folio.test.TestUtils.asJsonString;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.folio.login.service.PasswordService;
import org.folio.spring.exception.NotFoundException;
import org.folio.spring.integration.XOkapiHeaders;
import org.folio.test.types.UnitTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@UnitTest
@Import(ApiExceptionHandler.class)
@WebMvcTest(PasswordController.class)
class PasswordControllerTest {

  @Autowired private MockMvc mockMvc;
  @MockBean private PasswordService passwordService;

  @Test
  void createResetPasswordAction_positive() throws Exception {
    var passCreateAction = passwordCreateAction();
    var response =  responseCreateAction(true);
    when(passwordService.createResetPasswordAction(passCreateAction))
      .thenReturn(response);

    mockMvc.perform(post("/authn/password-reset-action")
        .contentType(APPLICATION_JSON)
        .header(XOkapiHeaders.TENANT, TENANT)
        .content(asJsonString(passCreateAction)))
      .andExpect(status().isCreated());
  }

  @Test
  void createResetPasswordAction_negative_userNotFound() throws Exception {
    var passCreateAction = passwordCreateAction();
    var errorMessage = "Keycloak user doesn't exist with the given 'user_id' attribute: " + USER_ID;
    var error = new NotFoundException(errorMessage);
    when(passwordService.createResetPasswordAction(passCreateAction))
      .thenThrow(error);

    mockMvc.perform(post("/authn/password-reset-action")
        .contentType(APPLICATION_JSON)
        .header(XOkapiHeaders.TENANT, TENANT)
        .content(asJsonString(passCreateAction)))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.errors[0].message", is(errorMessage)))
      .andExpect(jsonPath("$.errors[0].type", is("NotFoundException")))
      .andExpect(jsonPath("$.errors[0].code", is("not_found_error")));
  }

  @Test
  void createResetPasswordAction_negative_alreadyExist() throws Exception {
    var passCreateAction = passwordCreateAction();
    var errorMessage = "Password action with ID: " + PASSWORD_RESET_ACTION_ID
      + " already exist for a user: " + USER_ID;
    var error = new EntityExistsException(errorMessage);
    when(passwordService.createResetPasswordAction(passCreateAction))
      .thenThrow(error);

    mockMvc.perform(post("/authn/password-reset-action")
        .contentType(APPLICATION_JSON)
        .header(XOkapiHeaders.TENANT, TENANT)
        .content(asJsonString(passCreateAction)))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.errors[0].message", is(errorMessage)))
      .andExpect(jsonPath("$.errors[0].type", is("EntityExistsException")))
      .andExpect(jsonPath("$.errors[0].code", is("found_error")));
  }

  @Test
  void getPasswordActionById_positive() throws Exception {
    var passCreateAction = passwordCreateAction();
    when(passwordService.getPasswordCreateActionById(PASSWORD_RESET_ACTION_ID))
      .thenReturn(passCreateAction);

    mockMvc.perform(get("/authn/password-reset-action/{actionId}", PASSWORD_RESET_ACTION_ID)
        .contentType(APPLICATION_JSON)
        .header(XOkapiHeaders.TENANT, TENANT))
      .andExpect(status().isOk());
  }

  @Test
  void getPasswordActionById_negative_notFound() throws Exception {
    var errorMessage = "Password action with ID: " + PASSWORD_RESET_ACTION_ID + " was not found in the db";
    var error = new EntityNotFoundException(errorMessage);
    when(passwordService.getPasswordCreateActionById(PASSWORD_RESET_ACTION_ID))
      .thenThrow(error);

    mockMvc.perform(get("/authn/password-reset-action/{actionId}", PASSWORD_RESET_ACTION_ID)
        .contentType(APPLICATION_JSON)
        .header(XOkapiHeaders.TENANT, TENANT))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.errors[0].message", is(errorMessage)))
      .andExpect(jsonPath("$.errors[0].type", is("EntityNotFoundException")))
      .andExpect(jsonPath("$.errors[0].code", is("not_found_error")));
  }

  @Test
  void resetPassword_positive() throws Exception {
    var passResetAction = passwordResetAction();
    var response = responseResetAction(false);
    when(passwordService.resetAction(passResetAction))
      .thenReturn(response);

    mockMvc.perform(post("/authn/reset-password")
        .contentType(APPLICATION_JSON)
        .content(asJsonString(passResetAction))
        .header(XOkapiHeaders.TENANT, TENANT))
      .andExpect(status().isCreated());
  }
}
