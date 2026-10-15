package org.folio.login.integration.kafka;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.folio.login.integration.kafka.event.LogoutEvent.Type.LOGOUT;
import static org.folio.login.integration.kafka.event.LogoutEvent.Type.LOGOUT_ALL;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.folio.login.exception.TokenParsingException;
import org.folio.login.integration.kafka.configuration.property.KafkaProperties;
import org.folio.login.integration.kafka.event.LogoutEvent;
import org.folio.spring.FolioExecutionContext;
import org.folio.test.types.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

@UnitTest
@ExtendWith(MockitoExtension.class)
class LogoutEventPublisherTest {

  private static final String TOKEN =
    "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9."
      + "eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyLCJza"
      + "WQiOiIxMTExMTExMS0xMTExLTExMTEtMTExMS0xMTExMTExMTExMTEifQ.anXSx_64-BoS6gQmVwr1zqiDVZX1vjGKpnTSj1UFb8A";
  private static final String SESSION_ID = "11111111-1111-1111-1111-111111111111";

  @InjectMocks private LogoutEventPublisher logoutEventPublisher;

  @Mock private KafkaTemplate<String, LogoutEvent> kafkaTemplate;
  @Mock private FolioExecutionContext context;
  @Mock private KafkaProperties kafkaProperties;

  @Captor private ArgumentCaptor<ProducerRecord<String, LogoutEvent>> recordCaptor;

  @Test
  void publishLogoutEvent_positive() {
    var userId = UUID.randomUUID();
    var tenantId = "testtenant";
    when(context.getTenantId()).thenReturn(tenantId);
    when(context.getUserId()).thenReturn(userId);
    when(kafkaProperties.isProducerTenantCollection()).thenReturn(false);

    logoutEventPublisher.publishLogoutEvent(TOKEN);

    var expectedEvent = LogoutEvent.builder().userId(userId.toString()).sessionId(SESSION_ID).type(LOGOUT).build();
    var expectedMessageKey = userId.toString();
    var expectedTopicName = String.format("folio.%s.mod-login-keycloak.logout", tenantId);

    assertPublishedRecord(expectedTopicName, expectedMessageKey, expectedEvent, tenantId);
  }

  @Test
  void publishLogoutEvent_negative_tokenParsingException() {
    assertThatThrownBy(() -> logoutEventPublisher.publishLogoutEvent("invalidToken"))
      .isInstanceOf(TokenParsingException.class)
      .hasMessage("Cannot parse refresh token");
  }

  @Test
  void publishLogoutAllEvent_positive() {
    var userId = UUID.randomUUID();
    var tenantId = "testtenant";
    when(context.getTenantId()).thenReturn(tenantId);
    when(context.getUserId()).thenReturn(userId);
    when(kafkaProperties.isProducerTenantCollection()).thenReturn(false);

    var keycloakUserId = UUID.randomUUID();
    logoutEventPublisher.publishLogoutAllEvent(keycloakUserId.toString());

    var expectedEvent =
      LogoutEvent.builder()
        .userId(userId.toString())
        .keycloakUserId(keycloakUserId.toString())
        .type(LOGOUT_ALL)
        .build();
    var expectedMessageKey = userId.toString();
    var expectedTopicName = String.format("folio.%s.mod-login-keycloak.logout", tenantId);

    assertPublishedRecord(expectedTopicName, expectedMessageKey, expectedEvent, tenantId);
  }

  @ParameterizedTest
  @CsvSource({"ALL, testtenant", "COLLECTIONA, othertenant"})
  void publishLogoutEvent_positive_withTenantCollection(String collection, String tenantId) {
    var userId = UUID.randomUUID();
    when(context.getTenantId()).thenReturn(tenantId);
    when(context.getUserId()).thenReturn(userId);
    when(kafkaProperties.isProducerTenantCollection()).thenReturn(true);
    when(kafkaProperties.getTenantCollectionQualifier()).thenReturn(collection);

    logoutEventPublisher.publishLogoutEvent(TOKEN);

    var expectedEvent = LogoutEvent.builder().userId(userId.toString()).sessionId(SESSION_ID).type(LOGOUT).build();
    var expectedMessageKey = userId.toString();
    var expectedTopicName = "folio." + collection + ".mod-login-keycloak.logout";

    assertPublishedRecord(expectedTopicName, expectedMessageKey, expectedEvent, tenantId);
  }

  @ParameterizedTest
  @CsvSource({"ALL, testtenant", "COLLECTIONA, othertenant"})
  void publishLogoutAllEvent_positive_withTenantCollection(String collection, String tenantId) {
    var userId = UUID.randomUUID();
    when(context.getTenantId()).thenReturn(tenantId);
    when(context.getUserId()).thenReturn(userId);
    when(kafkaProperties.isProducerTenantCollection()).thenReturn(true);
    when(kafkaProperties.getTenantCollectionQualifier()).thenReturn(collection);

    var keycloakUserId = UUID.randomUUID();
    logoutEventPublisher.publishLogoutAllEvent(keycloakUserId.toString());

    var expectedEvent =
      LogoutEvent.builder()
        .userId(userId.toString())
        .keycloakUserId(keycloakUserId.toString())
        .type(LOGOUT_ALL)
        .build();
    var expectedMessageKey = userId.toString();
    var expectedTopicName = "folio." + collection + ".mod-login-keycloak.logout";

    assertPublishedRecord(expectedTopicName, expectedMessageKey, expectedEvent, tenantId);
  }

  private void assertPublishedRecord(String topic, String key, LogoutEvent event, String tenantId) {
    verify(kafkaTemplate).send(recordCaptor.capture());
    var record = recordCaptor.getValue();
    assertThat(record.topic()).isEqualTo(topic);
    assertThat(record.key()).isEqualTo(key);
    assertThat(record.value()).isEqualTo(event);
    assertThat(record.headers().headers("x-okapi-tenant")).singleElement()
      .satisfies(header -> assertThat(header.value()).isEqualTo(tenantId.getBytes(UTF_8)));
    assertThat(record.headers().headers("folio.tenantId")).singleElement()
      .satisfies(header -> assertThat(header.value()).isEqualTo(tenantId.getBytes(UTF_8)));
  }
}
