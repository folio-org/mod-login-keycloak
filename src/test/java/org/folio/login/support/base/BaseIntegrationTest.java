package org.folio.login.support.base;

import static org.folio.login.support.TestUtils.cleanUpCaches;
import static org.folio.test.TestUtils.asJsonString;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import lombok.extern.log4j.Log4j2;
import org.folio.test.base.BaseBackendIntegrationTest;
import org.folio.test.extensions.EnableKeycloak;
import org.folio.test.extensions.EnablePostgres;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@Log4j2
@EnablePostgres
@SpringBootTest
@EnableKeycloak
@ActiveProfiles("it")
@AutoConfigureMockMvc
public abstract class BaseIntegrationTest extends BaseBackendIntegrationTest {

  @Autowired
  private CacheManager cacheManager;

  @BeforeEach
  void setUp() {
    cleanUpCaches(cacheManager);
  }

  public static ResultActions attemptGet(String uri, Object... args) throws Exception {
    return mockMvc.perform(get(uri, args).contentType(APPLICATION_JSON));
  }

  protected static ResultActions attemptPost(String uri, Object body, Object... args) throws Exception {
    return mockMvc.perform(post(uri, args)
      .content(asJsonString(body))
      .contentType(APPLICATION_JSON));
  }

  protected static ResultActions attemptPut(String uri, Object body, Object... args) throws Exception {
    return mockMvc.perform(put(uri, args)
      .content(asJsonString(body))
      .contentType(APPLICATION_JSON));
  }

  protected static ResultActions attemptDelete(String uri, Object... args) throws Exception {
    return mockMvc.perform(delete(uri, args)
      .contentType(APPLICATION_JSON));
  }

  public static ResultActions doGet(String uri, Object... args) throws Exception {
    return attemptGet(uri, args).andExpect(status().isOk());
  }

  public static ResultActions doGet(MockHttpServletRequestBuilder request) throws Exception {
    return mockMvc.perform(request.contentType(APPLICATION_JSON)).andExpect(status().isOk());
  }

  protected static ResultActions doPost(String uri, Object body, Object... args) throws Exception {
    return attemptPost(uri, body, args).andExpect(status().isCreated());
  }

  protected static ResultActions doPut(String uri, Object body, Object... args) throws Exception {
    return attemptPut(uri, body, args).andExpect(status().isOk());
  }

  protected static ResultActions doDelete(String uri, Object... args) throws Exception {
    return attemptDelete(uri, args).andExpect(status().isNoContent());
  }
}
