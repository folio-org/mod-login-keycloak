package org.folio.login.support;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Durations.FIVE_SECONDS;
import static org.awaitility.Durations.ONE_HUNDRED_MILLISECONDS;
import static org.folio.common.utils.CollectionUtils.mapItems;
import static org.folio.integration.kafka.KafkaUtils.getTenantTopicName;
import static org.folio.login.integration.kafka.event.LogoutEvent.Type.LOGOUT;
import static org.folio.login.support.TestConstants.TENANT;
import static org.folio.login.support.TestUtils.extractJwtSessionId;
import static org.folio.test.FakeKafkaConsumer.getEvents;

import java.util.List;
import lombok.experimental.UtilityClass;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.awaitility.Awaitility;
import org.awaitility.core.ConditionFactory;
import org.folio.login.integration.kafka.event.LogoutEvent;
import org.folio.login.integration.kafka.event.LogoutEvent.Type;

@UtilityClass
public class TestKafkaUtils {

  public static String logoutTopic() {
    return getTenantTopicName("mod-login-keycloak.logout", TENANT);
  }

  public static LogoutEvent logoutEvent(String userId, String jwt) {
    return LogoutEvent.builder().type(LOGOUT).sessionId(extractJwtSessionId(jwt)).userId(userId).build();
  }

  public static LogoutEvent logoutAllEvent(String userId, String keycloakUserId) {
    return LogoutEvent.builder().type(Type.LOGOUT_ALL).userId(userId).keycloakUserId(keycloakUserId).build();
  }

  public static void assertLogoutEvents(LogoutEvent... events) {
    assertLogoutEvents(asList(events));
  }

  public static void assertLogoutEvents(List<LogoutEvent> events) {
    await().untilAsserted(() -> {
      var consumerRecords = getEvents(logoutTopic(), LogoutEvent.class);
      var entitlementEvents = mapItems(consumerRecords, ConsumerRecord::value);
      assertThat(entitlementEvents).containsAll(events);
    });
  }

  private static ConditionFactory await() {
    return Awaitility.await().atMost(FIVE_SECONDS).pollInterval(ONE_HUNDRED_MILLISECONDS);
  }
}
