package org.folio.login.it;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.awaitility.Durations.FIVE_SECONDS;
import static org.awaitility.Durations.ONE_HUNDRED_MILLISECONDS;
import static org.folio.integration.kafka.producer.KafkaUtils.createTopic;
import static org.folio.login.support.TestConstants.KEYCLOAK_USER_ID;
import static org.folio.login.support.TestConstants.REFRESH_TOKEN;
import static org.folio.login.support.TestKafkaUtils.assertTenantHeaders;
import static org.folio.login.support.TestKafkaUtils.logoutAllEvent;
import static org.folio.login.support.TestKafkaUtils.logoutEvent;
import static org.folio.test.FakeKafkaConsumer.getEvents;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.folio.common.utils.OkapiHeaders;
import org.folio.integration.kafka.producer.KafkaAdminService;
import org.folio.login.integration.kafka.LogoutEventPublisher;
import org.folio.login.integration.kafka.configuration.property.KafkaProperties;
import org.folio.login.integration.kafka.event.LogoutEvent;
import org.folio.login.support.base.BaseIntegrationTest;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.scope.FolioExecutionContextSetter;
import org.folio.test.types.IntegrationTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;

@IntegrationTest
class LogoutEventPublisherIT extends BaseIntegrationTest {

  @Autowired private KafkaTemplate<String, LogoutEvent> kafkaTemplate;
  @Autowired private FolioExecutionContext context;
  @Autowired private KafkaAdminService kafkaAdminService;

  @ParameterizedTest
  @CsvSource({"false, test, test", "ALL, test, ALL", "ALL, othertenant, ALL", "COLLECTIONA, othertenant, COLLECTIONA"})
  void publishLogoutEvents_positive_preservesTenantHeadersAndPayload(String collection, String tenant,
    String topicQualifier) {
    var properties = new KafkaProperties();
    properties.setProducerTenantCollection(collection);
    var publisher = new LogoutEventPublisher(properties, context, kafkaTemplate);
    var userId = UUID.randomUUID().toString();
    var topic = "folio." + topicQualifier + ".mod-login-keycloak.logout";
    kafkaAdminService.createTopic(createTopic(topic, 1, (short) 1));
    fakeKafkaConsumer.registerTopic(topic, LogoutEvent.class);
    Map<String, Collection<String>> headers = Map.of(
      OkapiHeaders.TENANT, List.of(tenant), OkapiHeaders.USER_ID, List.of(userId));

    try (var ignored = new FolioExecutionContextSetter(context.getFolioModuleMetadata(), headers)) {
      publisher.publishLogoutEvent(REFRESH_TOKEN);
      publisher.publishLogoutAllEvent(KEYCLOAK_USER_ID);
    }

    await().atMost(FIVE_SECONDS).pollInterval(ONE_HUNDRED_MILLISECONDS).untilAsserted(() -> {
      var records = getEvents(topic, LogoutEvent.class).stream()
        .filter(consumerRecord -> userId.equals(consumerRecord.key()))
        .toList();
      assertThat(records).extracting(ConsumerRecord::value)
        .containsExactly(logoutEvent(userId, REFRESH_TOKEN), logoutAllEvent(userId, KEYCLOAK_USER_ID));
      assertThat(records).allSatisfy(consumerRecord -> assertTenantHeaders(consumerRecord, tenant));
    });
  }
}
