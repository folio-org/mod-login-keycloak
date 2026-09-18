package org.folio.login.util;

import lombok.experimental.UtilityClass;
import org.folio.integration.kafka.producer.KafkaUtils;
import org.folio.login.integration.kafka.configuration.property.KafkaProperties;

@UtilityClass
public class KafkaTopicUtils {

  public static String getTopicName(String topicName, String tenantId, KafkaProperties kafkaProperties) {
    var tenantName = kafkaProperties.isProducerTenantCollection()
      ? kafkaProperties.getTenantCollectionQualifier()
      : tenantId;
    return KafkaUtils.getTenantTopicName(topicName, tenantName);
  }
}
