package org.folio.login.integration.kafka.configuration.property;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.folio.integration.kafka.producer.KafkaProducerProperties.KafkaTopic;
import org.folio.test.types.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

@UnitTest
class KafkaPropertiesTest {

  private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
    .withInitializer(new ConfigDataApplicationContextInitializer())
    .withUserConfiguration(TestConfiguration.class);

  @Test
  void producerTenantCollection_positive_disabledByDefault() {
    contextRunner.run(context -> {
      var properties = context.getBean(KafkaProperties.class);
      assertThat(properties.isProducerTenantCollection()).isFalse();
    });
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "false", "FALSE"})
  void producerTenantCollection_positive_disabled(String value) {
    contextRunner.withPropertyValues("KAFKA_PRODUCER_TENANT_COLLECTION=" + value).run(context -> {
      var properties = context.getBean(KafkaProperties.class);
      assertThat(properties.isProducerTenantCollection()).isFalse();
    });
  }

  @ParameterizedTest
  @CsvSource({"ALL, ALL", "COLLECTIONA, COLLECTIONA", "true, ALL", "TRUE, ALL"})
  void producerTenantCollection_positive_enabled(String value, String expectedQualifier) {
    contextRunner.withPropertyValues("KAFKA_PRODUCER_TENANT_COLLECTION=" + value).run(context -> {
      var properties = context.getBean(KafkaProperties.class);
      assertThat(properties.isProducerTenantCollection()).isTrue();
      assertThat(properties.getTenantCollectionQualifier()).isEqualTo(expectedQualifier);
    });
  }

  @ParameterizedTest
  @ValueSource(strings = {"all", "yes", "COLLECTION-A", "COLLECTION_A", "1ALL", "ABBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB"})
  void producerTenantCollection_negative_invalidValue(String value) {
    contextRunner.withPropertyValues("KAFKA_PRODUCER_TENANT_COLLECTION=" + value).run(context -> {
      assertThat(context).hasFailed();
      assertThat(context.getStartupFailure()).rootCause()
        .hasMessageContaining("must be a tenant collection name matching [A-Z][A-Z0-9]{0,30}");
    });
  }

  @Test
  void shouldBindTenantTopicsProperty() {
    contextRunner.withPropertyValues(
      "KAFKA_LOGOUT_TOPIC_PARTITIONS=3",
      "KAFKA_LOGOUT_TOPIC_REPLICATION_FACTOR=2"
    ).run(context -> {
      List<KafkaTopic> tenantTopics = context.getBean(KafkaProperties.class).getTenantTopics();

      assertThat(tenantTopics).hasSize(1);

      KafkaTopic topic = tenantTopics.get(0);
      assertThat(topic.getName()).isEqualTo("mod-login-keycloak.logout");
      assertThat(topic.getNumPartitions()).isEqualTo(3);
      assertThat(topic.getReplicationFactor()).isEqualTo((short) 2);
    });
  }

  @Test
  void shouldHandleNullProducerTenantCollection() {
    var properties = new KafkaProperties();
    assertThat(properties.isProducerTenantCollection()).isFalse();
  }

  @Test
  void shouldHandleNullTenantTopics() {
    var properties = new KafkaProperties();
    assertThat(properties.getTenantTopics()).isNull();
  }

  @EnableConfigurationProperties(KafkaProperties.class)
  static class TestConfiguration {}
}
