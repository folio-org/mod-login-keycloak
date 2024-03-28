package org.folio.login.it;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.login.domain.dto.LogEventType.FAILED_LOGIN_ATTEMPT;
import static org.folio.login.domain.dto.LogEventType.PASSWORD_RESET;
import static org.folio.login.domain.dto.LogEventType.SUCCESSFUL_LOGIN_ATTEMPT;
import static org.folio.login.support.TestConstants.TENANT;
import static org.folio.login.support.TestConstants.USER_ID;
import static org.folio.login.support.TestValues.invalidLoginCredentials;
import static org.folio.login.support.TestValues.loginCredentials;
import static org.folio.login.support.TestValues.updateCredentials;
import static org.folio.test.TestUtils.asJsonString;
import static org.folio.test.TestUtils.parseResponse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.stream.Stream;
import lombok.SneakyThrows;
import org.folio.login.domain.dto.LogEvent;
import org.folio.login.domain.dto.LogEventCollection;
import org.folio.login.domain.dto.LogEventType;
import org.folio.spring.integration.XOkapiHeaders;
import org.folio.test.base.BaseBackendIntegrationTest;
import org.folio.test.extensions.EnableKeycloakTlsMode;
import org.folio.test.extensions.EnablePostgres;
import org.folio.test.extensions.KeycloakRealms;
import org.folio.test.types.IntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@IntegrationTest
@EnablePostgres
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@EnableKeycloakTlsMode
@KeycloakRealms(realms = "/json/keycloak/test-realm.json")
class LogEventsIT extends BaseBackendIntegrationTest {

  @ParameterizedTest(name = "[{index}] {1}")
  @MethodSource("logEventsProvider")
  @DisplayName("testLogEvents_parametrized")
  void testLogEvents_loginEvent(Runnable eventTrigger, LogEventType expectedEventType) {
    eventTrigger.run();

    var logEvents = getUserEvents(1, 100);

    assertNotNull(logEvents);
    assertEquals(1, logEvents.getTotalRecords());
    assertEquals(1, logEvents.getLoggingEvent().size());

    var actualEvent = logEvents.getLoggingEvent().get(0);
    assertEquals(USER_ID, actualEvent.getUserId());
    assertEquals(TENANT, actualEvent.getTenant());
    assertEquals(expectedEventType, actualEvent.getEventType());
  }

  @Test
  void testLogEvents_multipleEvents() {
    callLoginEndpoint();
    callCredentialsUpdateEndpoint();
    callLoginEndpointWithInvalidCreds();

    var logEvents = getUserEvents(1, 100);

    assertNotNull(logEvents);
    assertEquals(3, logEvents.getTotalRecords());

    assertThat(logEvents.getLoggingEvent()).hasSize(3)
      .allMatch(event -> USER_ID.equals(event.getUserId()) && TENANT.equals(event.getTenant()))
      .map(LogEvent::getEventType)
      .containsExactly(FAILED_LOGIN_ATTEMPT, PASSWORD_RESET, SUCCESSFUL_LOGIN_ATTEMPT);
  }

  @Test
  void testLogEvents_pagination() {
    callLoginEndpoint();
    callCredentialsUpdateEndpoint();
    callLoginEndpointWithInvalidCreds();

    var logEvents = getUserEvents(2, 1);

    assertNotNull(logEvents);
    assertEquals(1, logEvents.getTotalRecords());
    assertEquals(1, logEvents.getLoggingEvent().size());

    var actualEvent = logEvents.getLoggingEvent().get(0);
    assertEquals(USER_ID, actualEvent.getUserId());
    assertEquals(TENANT, actualEvent.getTenant());
    assertEquals(PASSWORD_RESET, actualEvent.getEventType());
  }

  @Test
  void testLogEvents_pagination_exceededOffset() {
    callLoginEndpoint();

    var logEvents = getUserEvents(2, 100);

    assertNotNull(logEvents);
    assertEquals(0, logEvents.getTotalRecords());
    assertEquals(0, logEvents.getLoggingEvent().size());
  }

  @Test
  void testLogEvents_noEvents() {
    var logEvents = getUserEvents(1, 100);
    assertEquals(0, logEvents.getTotalRecords());
  }

  @SneakyThrows
  private static void callLoginEndpoint() {
    mockMvc.perform(post("/authn/login")
        .contentType(APPLICATION_JSON)
        .header(XOkapiHeaders.TENANT, TENANT)
        .content(asJsonString(loginCredentials())))
      .andExpect(status().isCreated());
  }

  @SneakyThrows
  private static void callLoginEndpointWithInvalidCreds() {
    mockMvc.perform(post("/authn/login")
        .contentType(APPLICATION_JSON)
        .header(XOkapiHeaders.TENANT, TENANT)
        .content(asJsonString(invalidLoginCredentials())))
      .andExpect(status().isBadRequest());
  }

  @SneakyThrows
  private static void callCredentialsUpdateEndpoint() {
    mockMvc.perform(post("/authn/update")
        .content(asJsonString(updateCredentials()))
        .header(CONTENT_TYPE, APPLICATION_JSON)
        .header(XOkapiHeaders.TENANT, TENANT))
      .andExpect(status().isNoContent());
  }

  private static Stream<Arguments> logEventsProvider() {
    return Stream.of(
      Arguments.arguments((Runnable) LogEventsIT::callLoginEndpoint, SUCCESSFUL_LOGIN_ATTEMPT),
      Arguments.arguments((Runnable) LogEventsIT::callCredentialsUpdateEndpoint, PASSWORD_RESET),
      Arguments.arguments((Runnable) LogEventsIT::callLoginEndpointWithInvalidCreds, FAILED_LOGIN_ATTEMPT)
    );
  }

  @SneakyThrows
  private static LogEventCollection getUserEvents(int start, int limit) {
    var mvcResult = mockMvc.perform(get("/authn/log/events")
        .contentType(APPLICATION_JSON)
        .queryParam("start", String.valueOf(start))
        .queryParam("length", String.valueOf(limit))
        .header(XOkapiHeaders.TENANT, TENANT))
      .andExpect(status().isOk())
      .andReturn();
    return parseResponse(mvcResult, LogEventCollection.class);
  }
}
