package org.folio.login.integration.kafka.configuration.property;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.folio.integration.kafka.FolioKafkaProperties.KafkaTopic;
import org.folio.test.types.UnitTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@UnitTest
@SpringBootTest(classes = KafkaProperties.class)
@EnableConfigurationProperties(KafkaProperties.class)
@TestPropertySource(properties = {
  "application.kafka.producer-tenant-collection=true",
  "application.kafka.tenant-topics[0].name=mod-login-keycloak.logout",
  "application.kafka.tenant-topics[0].num-partitions=3",
  "application.kafka.tenant-topics[0].replication-factor=2"
})
class KafkaPropertiesTest {

  @Autowired
  private KafkaProperties kafkaProperties;

  @Test
  void shouldBindProducerTenantCollectionProperty() {
    assertThat(kafkaProperties.getProducerTenantCollection()).isTrue();
  }

  @Test
  void shouldBindTenantTopicsProperty() {
    List<KafkaTopic> tenantTopics = kafkaProperties.getTenantTopics();

    assertThat(tenantTopics).hasSize(1);

    KafkaTopic topic = tenantTopics.get(0);
    assertThat(topic.getName()).isEqualTo("mod-login-keycloak.logout");
    assertThat(topic.getNumPartitions()).isEqualTo(3);
    assertThat(topic.getReplicationFactor()).isEqualTo((short) 2);
  }

  @Test
  void shouldHandleNullProducerTenantCollection() {
    var properties = new KafkaProperties();
    assertThat(properties.getProducerTenantCollection()).isNull();
  }

  @Test
  void shouldHandleNullTenantTopics() {
    var properties = new KafkaProperties();
    assertThat(properties.getTenantTopics()).isNull();
  }
}
