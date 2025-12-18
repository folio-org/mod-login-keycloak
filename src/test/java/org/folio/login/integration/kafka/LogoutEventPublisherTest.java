package org.folio.login.integration.kafka;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.folio.login.integration.kafka.event.LogoutEvent.Type.LOGOUT;
import static org.folio.login.integration.kafka.event.LogoutEvent.Type.LOGOUT_ALL;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;
import org.folio.login.exception.TokenParsingException;
import org.folio.login.integration.kafka.configuration.property.KafkaProperties;
import org.folio.login.integration.kafka.event.LogoutEvent;
import org.folio.spring.FolioExecutionContext;
import org.folio.test.types.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

  @Test
  void publishLogoutEvent_positive() {
    var userId = UUID.randomUUID();
    var tenantId = "testtenant";
    when(context.getTenantId()).thenReturn(tenantId);
    when(context.getUserId()).thenReturn(userId);
    when(kafkaProperties.getProducerTenantCollection()).thenReturn(false);

    logoutEventPublisher.publishLogoutEvent(TOKEN);

    var expectedEvent = LogoutEvent.builder().userId(userId.toString()).sessionId(SESSION_ID).type(LOGOUT).build();
    var expectedMessageKey = userId.toString();
    var expectedTopicName = String.format("folio.%s.mod-login-keycloak.logout", tenantId);

    verify(kafkaTemplate).send(expectedTopicName, expectedMessageKey, expectedEvent);
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
    when(kafkaProperties.getProducerTenantCollection()).thenReturn(false);

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

    verify(kafkaTemplate).send(expectedTopicName, expectedMessageKey, expectedEvent);
  }

  @Test
  void publishLogoutEvent_withTenantCollection_positive() {
    var userId = UUID.randomUUID();
    when(context.getUserId()).thenReturn(userId);
    when(kafkaProperties.getProducerTenantCollection()).thenReturn(true);

    logoutEventPublisher.publishLogoutEvent(TOKEN);

    var expectedEvent = LogoutEvent.builder().userId(userId.toString()).sessionId(SESSION_ID).type(LOGOUT).build();
    var expectedMessageKey = userId.toString();
    var expectedTopicName = "folio.ALL.mod-login-keycloak.logout";

    verify(kafkaTemplate).send(expectedTopicName, expectedMessageKey, expectedEvent);
  }

  @Test
  void publishLogoutAllEvent_withTenantCollection_positive() {
    var userId = UUID.randomUUID();
    when(context.getUserId()).thenReturn(userId);
    when(kafkaProperties.getProducerTenantCollection()).thenReturn(true);

    var keycloakUserId = UUID.randomUUID();
    logoutEventPublisher.publishLogoutAllEvent(keycloakUserId.toString());

    var expectedEvent =
      LogoutEvent.builder()
        .userId(userId.toString())
        .keycloakUserId(keycloakUserId.toString())
        .type(LOGOUT_ALL)
        .build();
    var expectedMessageKey = userId.toString();
    var expectedTopicName = "folio.ALL.mod-login-keycloak.logout";

    verify(kafkaTemplate).send(expectedTopicName, expectedMessageKey, expectedEvent);
  }
}
