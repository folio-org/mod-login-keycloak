package org.folio.login.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.folio.login.integration.kafka.configuration.property.KafkaProperties;
import org.folio.test.types.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@UnitTest
class KafkaTopicUtilsTest {

  private static final String TOPIC_NAME = "mod-login-keycloak.logout";
  private static final String TENANT_ID = "testtenant";

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"false", "FALSE"})
  void getTopicName_withProducerTenantCollectionDisabled(String producerTenantCollection) {
    var result = KafkaTopicUtils.getTopicName(TOPIC_NAME, TENANT_ID, kafkaProperties(producerTenantCollection));

    assertThat(result).isEqualTo("folio.testtenant.mod-login-keycloak.logout");
  }

  @ParameterizedTest
  @CsvSource({"true, ALL", "TRUE, ALL", "ALL, ALL", "COLLECTIONA, COLLECTIONA"})
  void getTopicName_withProducerTenantCollectionEnabled(String producerTenantCollection, String tenantCollection) {
    var result = KafkaTopicUtils.getTopicName(TOPIC_NAME, TENANT_ID, kafkaProperties(producerTenantCollection));

    assertThat(result).isEqualTo("folio." + tenantCollection + ".mod-login-keycloak.logout");
  }

  @Test
  void getTopicName_withDifferentTenantId() {
    var result = KafkaTopicUtils.getTopicName(TOPIC_NAME, "anothertenant", kafkaProperties("false"));

    assertThat(result).isEqualTo("folio.anothertenant.mod-login-keycloak.logout");
  }

  @Test
  void getTopicName_withDifferentTopicName() {
    var topicName = "mod-login-keycloak.events";
    var result = KafkaTopicUtils.getTopicName(topicName, TENANT_ID, kafkaProperties("false"));

    assertThat(result).isEqualTo("folio.testtenant.mod-login-keycloak.events");
  }

  @Test
  void getTopicName_tenantCollectionIgnoresTenantId() {
    var result1 = KafkaTopicUtils.getTopicName(TOPIC_NAME, "tenant1", kafkaProperties("ALL"));
    var result2 = KafkaTopicUtils.getTopicName(TOPIC_NAME, "tenant2", kafkaProperties("ALL"));

    assertThat(result1).isEqualTo("folio.ALL.mod-login-keycloak.logout");
    assertThat(result2).isEqualTo("folio.ALL.mod-login-keycloak.logout");
    assertThat(result1).isEqualTo(result2);
  }

  private static KafkaProperties kafkaProperties(String producerTenantCollection) {
    var kafkaProperties = new KafkaProperties();
    kafkaProperties.setProducerTenantCollection(producerTenantCollection);
    return kafkaProperties;
  }
}
