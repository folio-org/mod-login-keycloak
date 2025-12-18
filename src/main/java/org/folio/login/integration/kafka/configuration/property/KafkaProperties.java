package org.folio.login.integration.kafka.configuration.property;

import java.util.List;
import lombok.Data;
import org.folio.integration.kafka.FolioKafkaProperties.KafkaTopic;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("application.kafka")
public class KafkaProperties {

  private Boolean producerTenantCollection;

  @NestedConfigurationProperty
  private List<KafkaTopic> tenantTopics;
}
