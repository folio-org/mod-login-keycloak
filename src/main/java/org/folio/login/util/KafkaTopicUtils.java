package org.folio.login.util;

import lombok.experimental.UtilityClass;
import org.folio.integration.kafka.KafkaUtils;

@UtilityClass
public class KafkaTopicUtils {

  private static final String ALL_TENANTS = "ALL";

  public static String getTopicName(String topicName, String tenantId, boolean producerTenantCollection) {
    var tenantName = producerTenantCollection
      ? ALL_TENANTS
      : tenantId;
    return KafkaUtils.getTenantTopicName(topicName, tenantName);
  }
}
