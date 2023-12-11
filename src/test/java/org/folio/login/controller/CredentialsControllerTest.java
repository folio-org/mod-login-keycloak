package org.folio.login.controller;

import static org.folio.login.support.TestConstants.TENANT;
import static org.folio.login.support.TestConstants.USER_ID;
import static org.folio.login.support.TestValues.loginCredentials;
import static org.folio.login.support.TestValues.updateCredentials;
import static org.folio.test.TestUtils.asJsonString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.folio.login.domain.dto.CredentialsExistence;
import org.folio.login.service.CredentialsService;
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
@WebMvcTest(CredentialsController.class)
class CredentialsControllerTest {

  @Autowired private MockMvc mockMvc;
  @MockBean private CredentialsService credentialsService;

  @Test
  void createCredentials_positive() throws Exception {
    var credentials = loginCredentials();
    doNothing().when(credentialsService).createAuthCredentials(credentials);

    mockMvc.perform(post("/authn/credentials")
        .contentType(APPLICATION_JSON)
        .header(XOkapiHeaders.TENANT, TENANT)
        .content(asJsonString(credentials)))
      .andExpect(status().isCreated());
  }

  @Test
  void checkExistence_positive() throws Exception {
    when(credentialsService.checkCredentialsExistence(USER_ID))
      .thenReturn(new CredentialsExistence().credentialsExist(true));

    mockMvc.perform(get("/authn/credentials-existence")
        .param("userId", USER_ID)
        .contentType(APPLICATION_JSON)
        .header(XOkapiHeaders.TENANT, TENANT))
      .andExpect(status().isOk());
  }

  @Test
  void deleteCredentials_positive() throws Exception {
    doNothing().when(credentialsService).deleteAuthCredentials(USER_ID);

    mockMvc.perform(delete("/authn/credentials")
        .param("userId", USER_ID)
        .contentType(APPLICATION_JSON)
        .header(XOkapiHeaders.TENANT, TENANT))
      .andExpect(status().isNoContent());
  }

  @Test
  void updateCredentials_positive() throws Exception {
    var credentials = updateCredentials();
    doNothing().when(credentialsService).updateCredentials(credentials, null, null);

    mockMvc.perform(post("/authn/update")
        .contentType(APPLICATION_JSON)
        .header(XOkapiHeaders.TENANT, TENANT)
        .content(asJsonString(credentials)))
      .andExpect(status().isNoContent());
  }
}
