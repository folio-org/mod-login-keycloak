package org.folio.login.it;

import static org.assertj.core.api.Assertions.assertThat;

import org.folio.integration.kafka.FolioKafkaProperties.KafkaTopic;
import org.folio.login.integration.kafka.configuration.property.KafkaProperties;
import org.folio.login.support.base.BaseIntegrationTest;
import org.folio.test.types.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
class KafkaPropertiesIT extends BaseIntegrationTest {

  @Autowired
  private KafkaProperties kafkaProperties;

  @Test
  void shouldLoadKafkaPropertiesFromConfiguration() {
    assertThat(kafkaProperties).isNotNull();
    assertThat(kafkaProperties.isProducerTenantCollection()).isFalse();
    assertThat(kafkaProperties.getTenantTopics()).isNotNull();
  }

  @Test
  void shouldLoadTenantTopicsFromConfiguration() {
    var tenantTopics = kafkaProperties.getTenantTopics();

    assertThat(tenantTopics).isNotNull().isNotEmpty();

    var logoutTopic = tenantTopics.stream()
      .filter(topic -> "mod-login-keycloak.logout".equals(topic.getName()))
      .findFirst();

    assertThat(logoutTopic).isPresent();

    KafkaTopic topic = logoutTopic.get();
    assertThat(topic.getName()).isEqualTo("mod-login-keycloak.logout");
    assertThat(topic.getNumPartitions()).isEqualTo(1);
  }

  @Test
  void shouldUseEnvironmentVariableForProducerTenantCollection() {
    assertThat(kafkaProperties.isProducerTenantCollection()).isFalse();
  }
}
