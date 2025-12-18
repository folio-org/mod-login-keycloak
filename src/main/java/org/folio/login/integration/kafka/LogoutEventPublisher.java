package org.folio.login.integration.kafka;

import static org.folio.login.integration.kafka.event.LogoutEvent.Type.LOGOUT;
import static org.folio.login.integration.kafka.event.LogoutEvent.Type.LOGOUT_ALL;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.integration.kafka.KafkaUtils;
import org.folio.login.exception.TokenParsingException;
import org.folio.login.integration.kafka.configuration.property.KafkaProperties;
import org.folio.login.integration.kafka.event.LogoutEvent;
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
  private static final String ALL_TENANTS = "ALL";

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
    var tenantName = kafkaProperties.getProducerTenantCollection()
      ? ALL_TENANTS
      : context.getTenantId();
    return KafkaUtils.getTenantTopicName(TOPIC_NAME, tenantName);
  }

  private String getMessageKey() {
    return context.getUserId().toString();
  }

  private void send(LogoutEvent body) {
    kafkaTemplate.send(getTopicName(), getMessageKey(), body);
    log.debug("Sent event to topic: {}", this::getTopicName);
  }
}
