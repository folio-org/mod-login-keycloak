package org.folio.login.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.folio.test.types.UnitTest;
import org.junit.jupiter.api.Test;

@UnitTest
class KafkaTopicUtilsTest {

  private static final String TOPIC_NAME = "mod-login-keycloak.logout";
  private static final String TENANT_ID = "testtenant";

  @Test
  void getTopicName_withProducerTenantCollectionFalse() {
    var result = KafkaTopicUtils.getTopicName(TOPIC_NAME, TENANT_ID, false);

    assertThat(result).isEqualTo("folio.testtenant.mod-login-keycloak.logout");
  }

  @Test
  void getTopicName_withProducerTenantCollectionTrue() {
    var result = KafkaTopicUtils.getTopicName(TOPIC_NAME, TENANT_ID, true);

    assertThat(result).isEqualTo("folio.ALL.mod-login-keycloak.logout");
  }

  @Test
  void getTopicName_withDifferentTenantId() {
    var result = KafkaTopicUtils.getTopicName(TOPIC_NAME, "anothertenant", false);

    assertThat(result).isEqualTo("folio.anothertenant.mod-login-keycloak.logout");
  }

  @Test
  void getTopicName_withDifferentTopicName() {
    var topicName = "mod-login-keycloak.events";
    var result = KafkaTopicUtils.getTopicName(topicName, TENANT_ID, false);

    assertThat(result).isEqualTo("folio.testtenant.mod-login-keycloak.events");
  }

  @Test
  void getTopicName_tenantCollectionIgnoresTenantId() {
    var result1 = KafkaTopicUtils.getTopicName(TOPIC_NAME, "tenant1", true);
    var result2 = KafkaTopicUtils.getTopicName(TOPIC_NAME, "tenant2", true);

    assertThat(result1).isEqualTo("folio.ALL.mod-login-keycloak.logout");
    assertThat(result2).isEqualTo("folio.ALL.mod-login-keycloak.logout");
    assertThat(result1).isEqualTo(result2);
  }
}
