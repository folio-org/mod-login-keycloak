package org.folio.login.service;

import static org.folio.common.utils.CollectionUtils.toStream;
import static org.folio.integration.kafka.KafkaUtils.createTopic;
import static org.folio.integration.kafka.KafkaUtils.getTenantTopicName;

import lombok.extern.log4j.Log4j2;
import org.folio.integration.kafka.FolioKafkaProperties.KafkaTopic;
import org.folio.integration.kafka.KafkaAdminService;
import org.folio.login.integration.kafka.configuration.property.KafkaProperties;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.liquibase.FolioSpringLiquibase;
import org.folio.spring.service.TenantService;
import org.folio.tenant.domain.dto.TenantAttributes;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Log4j2
@Primary
@Service
public class ModuleCustomTenantService extends TenantService {

  private final KafkaAdminService kafkaAdminService;
  private final KafkaProperties kafkaProperties;
  private final FolioExecutionContext context;

  public ModuleCustomTenantService(JdbcTemplate jdbcTemplate, FolioExecutionContext context,
    FolioSpringLiquibase folioSpringLiquibase, KafkaAdminService kafkaAdminService, KafkaProperties kafkaProperties) {

    super(jdbcTemplate, context, folioSpringLiquibase);
    this.kafkaAdminService = kafkaAdminService;
    this.kafkaProperties = kafkaProperties;
    this.context = context;
  }

  @Override
  public void afterTenantUpdate(TenantAttributes tenantAttributes) {
    toStream(kafkaProperties.getTenantTopics()).forEach(this::createTenantTopics);
  }

  private void createTenantTopics(KafkaTopic tenantTopic) {
    var topicName = getTenantTopicName(tenantTopic.getName(), context.getTenantId());
    var topic = createTopic(topicName, tenantTopic.getNumPartitions(), tenantTopic.getReplicationFactor());
    kafkaAdminService.createTopic(topic);

    log.debug("Created topic: {}", topicName);
  }
}
