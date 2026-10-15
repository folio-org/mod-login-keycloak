package org.folio.login.integration.kafka;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.folio.login.integration.kafka.event.LogoutEvent.Type.LOGOUT;
import static org.folio.login.integration.kafka.event.LogoutEvent.Type.LOGOUT_ALL;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.folio.common.utils.OkapiHeaders;
import org.folio.login.exception.TokenParsingException;
import org.folio.login.integration.kafka.configuration.property.KafkaProperties;
import org.folio.login.integration.kafka.event.LogoutEvent;
import org.folio.login.util.KafkaTopicUtils;
import org.folio.spring.FolioExecutionContext;
import org.keycloak.jose.jws.JWSInputException;
import org.keycloak.util.TokenUtil;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class LogoutEventPublisher {

  private static final String TOPIC_NAME = "mod-login-keycloak.logout";

  private final KafkaProperties kafkaProperties;
  private final FolioExecutionContext context;
  private final KafkaTemplate<String, LogoutEvent> kafkaTemplate;

  public void publishLogoutEvent(String refreshToken) {
    try {
      var token = TokenUtil.getRefreshToken(refreshToken);
      var userId = context.getUserId().toString();
      var sessionId = token.getSessionId();
      var event = LogoutEvent.builder().userId(userId).sessionId(sessionId).type(LOGOUT).build();
      send(event);
    } catch (JWSInputException e) {
      log.debug("Cannot parse token", e);
      throw new TokenParsingException("Cannot parse refresh token");
    }
  }

  public void publishLogoutAllEvent(String keycloakUserId) {
    var userId = context.getUserId().toString();
    var event = LogoutEvent.builder().userId(userId).keycloakUserId(keycloakUserId).type(LOGOUT_ALL).build();
    send(event);
  }

  private String getTopicName() {
    return KafkaTopicUtils.getTopicName(TOPIC_NAME, context.getTenantId(), kafkaProperties);
  }

  private String getMessageKey() {
    return context.getUserId().toString();
  }

  private void send(LogoutEvent body) {
    var tenantBytes = context.getTenantId().getBytes(UTF_8);
    List<Header> kafkaHeaders = List.of(
      new RecordHeader(OkapiHeaders.TENANT, tenantBytes),
      new RecordHeader("folio.tenantId", tenantBytes));
    var producerRecord = new ProducerRecord<>(getTopicName(), null, getMessageKey(), body, kafkaHeaders);
    kafkaTemplate.send(producerRecord);
    log.debug("Sent event to topic: {}", this::getTopicName);
  }
}
