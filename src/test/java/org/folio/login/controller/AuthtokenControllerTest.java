package org.folio.login.controller;

import static org.folio.test.TestUtils.asJsonString;
import static org.hamcrest.Matchers.is;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import java.util.UUID;
import org.folio.spring.integration.XOkapiHeaders;
import org.folio.test.types.UnitTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.test.web.servlet.MockMvc;

@UnitTest
@MockBean(KafkaAdmin.class)
@Import(ApiExceptionHandler.class)
@WebMvcTest(AuthtokenController.class)
class AuthtokenControllerTest {

  private static final String TENANT = "test-tenant";
  private static final String OKAPI_URL = "https://test-okapi:9130";

  @Autowired private MockMvc mockMvc;

  @Test
  void tokenInvalidate_negative() throws Exception {
    mockMvc.perform(post("/token/invalidate")
        .contentType(APPLICATION_JSON)
        .header(XOkapiHeaders.TENANT, TENANT)
        .header(XOkapiHeaders.URL, OKAPI_URL)
        .content(asJsonString(Map.of("refreshToken", "token"))))
      .andExpect(status().isNotImplemented())
      .andExpect(jsonPath("$.total_records", is(1)))
      .andExpect(jsonPath("$.errors[0].message", is("tokenInvalidate() method is not implemented")))
      .andExpect(jsonPath("$.errors[0].type", is("UnsupportedOperationException")))
      .andExpect(jsonPath("$.errors[0].code", is("service_error")));
  }

  @Test
  void tokenInvalidateAll_negative() throws Exception {
    mockMvc.perform(post("/token/invalidate-all")
        .contentType(APPLICATION_JSON)
        .header(XOkapiHeaders.TENANT, TENANT)
        .header(XOkapiHeaders.URL, OKAPI_URL))
      .andExpect(status().isNotImplemented())
      .andExpect(jsonPath("$.total_records", is(1)))
      .andExpect(jsonPath("$.errors[0].message", is("tokenInvalidateAll() method is not implemented")))
      .andExpect(jsonPath("$.errors[0].type", is("UnsupportedOperationException")))
      .andExpect(jsonPath("$.errors[0].code", is("service_error")));
  }

  @Test
  void tokenLegacy_negative() throws Exception {
    mockMvc.perform(post("/token")
        .contentType(APPLICATION_JSON)
        .header(XOkapiHeaders.TENANT, TENANT)
        .header(XOkapiHeaders.URL, OKAPI_URL)
        .content(asJsonString(Map.of("payload", Map.of("sub", "test-sub")))))
      .andExpect(status().isNotImplemented())
      .andExpect(jsonPath("$.total_records", is(1)))
      .andExpect(jsonPath("$.errors[0].message", is("tokenLegacy() method is not implemented")))
      .andExpect(jsonPath("$.errors[0].type", is("UnsupportedOperationException")))
      .andExpect(jsonPath("$.errors[0].code", is("service_error")));
  }

  @Test
  void tokenRefresh_negative() throws Exception {
    mockMvc.perform(post("/token/refresh")
        .contentType(APPLICATION_JSON)
        .header(XOkapiHeaders.TENANT, TENANT)
        .header(XOkapiHeaders.URL, OKAPI_URL)
        .content(asJsonString(Map.of("refreshToken", "token"))))
      .andExpect(status().isNotImplemented())
      .andExpect(jsonPath("$.total_records", is(1)))
      .andExpect(jsonPath("$.errors[0].message", is("tokenRefresh() method is not implemented")))
      .andExpect(jsonPath("$.errors[0].type", is("UnsupportedOperationException")))
      .andExpect(jsonPath("$.errors[0].code", is("service_error")));
  }

  @Test
  void tokenSign_negative() throws Exception {
    mockMvc.perform(post("/token/sign")
        .contentType(APPLICATION_JSON)
        .header(XOkapiHeaders.TENANT, TENANT)
        .header(XOkapiHeaders.URL, OKAPI_URL)
        .content(asJsonString(Map.of("payload", Map.of("sub", "test-sub")))))
      .andExpect(status().isNotImplemented())
      .andExpect(jsonPath("$.total_records", is(1)))
      .andExpect(jsonPath("$.errors[0].message", is("tokenSign() method is not implemented")))
      .andExpect(jsonPath("$.errors[0].type", is("UnsupportedOperationException")))
      .andExpect(jsonPath("$.errors[0].code", is("service_error")));
  }

  @Test
  void tokenSignLegacy_negative() throws Exception {
    var userId = UUID.randomUUID();
    mockMvc.perform(post("/refreshtoken")
        .contentType(APPLICATION_JSON)
        .header(XOkapiHeaders.TENANT, TENANT)
        .header(XOkapiHeaders.URL, OKAPI_URL)
        .content(asJsonString(Map.of("userId", userId, "sub", "test-sub"))))
      .andExpect(status().isNotImplemented())
      .andExpect(jsonPath("$.total_records", is(1)))
      .andExpect(jsonPath("$.errors[0].message", is("tokenSignLegacy() method is not implemented")))
      .andExpect(jsonPath("$.errors[0].type", is("UnsupportedOperationException")))
      .andExpect(jsonPath("$.errors[0].code", is("service_error")));
  }
}
