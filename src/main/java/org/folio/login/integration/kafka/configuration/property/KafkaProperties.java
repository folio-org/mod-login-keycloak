package org.folio.login.integration.kafka.configuration.property;

import static org.apache.commons.lang3.StringUtils.isEmpty;

import jakarta.validation.constraints.Pattern;
import java.util.List;
import lombok.Data;
import org.folio.integration.kafka.producer.KafkaProducerProperties.KafkaTopic;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@Component
@ConfigurationProperties("application.kafka")
public class KafkaProperties {

  private static final String ALL_TENANTS = "ALL";

  /**
   * Tenant collection name for tenant topics, per-tenant topics are used if empty or {@code false}.
   *
   * <p>{@code true} is supported for backward compatibility and means the {@code ALL} tenant collection.</p>
   */
  @Pattern(regexp = "(?i:true|false)?|[A-Z][A-Z0-9]{0,30}",
    message = "must be a tenant collection name matching [A-Z][A-Z0-9]{0,30}")
  private String producerTenantCollection;

  @NestedConfigurationProperty
  private List<KafkaTopic> tenantTopics;

  public boolean isProducerTenantCollection() {
    return !isEmpty(producerTenantCollection) && !"false".equalsIgnoreCase(producerTenantCollection);
  }

  public String getTenantCollectionQualifier() {
    return "true".equalsIgnoreCase(producerTenantCollection) ? ALL_TENANTS : producerTenantCollection;
  }
}
