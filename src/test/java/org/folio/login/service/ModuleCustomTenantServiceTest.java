package org.folio.login.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.apache.kafka.clients.admin.NewTopic;
import org.folio.integration.kafka.FolioKafkaProperties.KafkaTopic;
import org.folio.integration.kafka.KafkaAdminService;
import org.folio.login.integration.kafka.configuration.property.KafkaProperties;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.liquibase.FolioSpringLiquibase;
import org.folio.tenant.domain.dto.TenantAttributes;
import org.folio.test.types.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

@UnitTest
@ExtendWith(MockitoExtension.class)
class ModuleCustomTenantServiceTest {

  @InjectMocks private ModuleCustomTenantService moduleCustomTenantService;

  @Mock private JdbcTemplate jdbcTemplate;
  @Mock private FolioExecutionContext context;
  @Mock private FolioSpringLiquibase folioSpringLiquibase;
  @Mock private KafkaAdminService kafkaAdminService;
  @Mock private KafkaProperties kafkaProperties;

  @Test
  void afterTenantUpdate_shouldCreateTenantTopics() {
    var topicName = "mod-login-keycloak.logout";
    var numPartitions = 1;
    var replicationFactor = (short) 1;

    var kafkaTopic = new KafkaTopic();
    kafkaTopic.setName(topicName);
    kafkaTopic.setNumPartitions(numPartitions);
    kafkaTopic.setReplicationFactor(replicationFactor);

    var tenantId = "testtenant";
    when(context.getTenantId()).thenReturn(tenantId);
    when(kafkaProperties.getTenantTopics()).thenReturn(List.of(kafkaTopic));
    when(kafkaProperties.getProducerTenantCollection()).thenReturn(false);

    var tenantAttributes = new TenantAttributes();
    moduleCustomTenantService.afterTenantUpdate(tenantAttributes);

    var topicCaptor = ArgumentCaptor.forClass(NewTopic.class);
    verify(kafkaAdminService).createTopic(topicCaptor.capture());

    var createdTopic = topicCaptor.getValue();
    var expectedTopicName = String.format("folio.%s.%s", tenantId, topicName);
    assertThat(createdTopic.name()).isEqualTo(expectedTopicName);
    assertThat(createdTopic.numPartitions()).isEqualTo(numPartitions);
    assertThat(createdTopic.replicationFactor()).isEqualTo(replicationFactor);
  }

  @Test
  void afterTenantUpdate_shouldCreateMultipleTenantTopics() {
    var kafkaTopic1 = new KafkaTopic();
    kafkaTopic1.setName("mod-login-keycloak.logout");
    kafkaTopic1.setNumPartitions(1);
    kafkaTopic1.setReplicationFactor((short) 1);

    var kafkaTopic2 = new KafkaTopic();
    kafkaTopic2.setName("mod-login-keycloak.events");
    kafkaTopic2.setNumPartitions(3);
    kafkaTopic2.setReplicationFactor((short) 2);

    var tenantId = "testtenant";
    when(context.getTenantId()).thenReturn(tenantId);
    when(kafkaProperties.getTenantTopics()).thenReturn(List.of(kafkaTopic1, kafkaTopic2));
    when(kafkaProperties.getProducerTenantCollection()).thenReturn(false);

    var tenantAttributes = new TenantAttributes();
    moduleCustomTenantService.afterTenantUpdate(tenantAttributes);

    verify(kafkaAdminService, org.mockito.Mockito.times(2)).createTopic(any(NewTopic.class));
  }

  @Test
  void afterTenantUpdate_shouldHandleEmptyTenantTopics() {
    when(kafkaProperties.getTenantTopics()).thenReturn(List.of());

    var tenantAttributes = new TenantAttributes();
    moduleCustomTenantService.afterTenantUpdate(tenantAttributes);

    verify(kafkaAdminService, org.mockito.Mockito.never()).createTopic(any());
  }

  @Test
  void afterTenantUpdate_shouldHandleNullTenantTopics() {
    when(kafkaProperties.getTenantTopics()).thenReturn(null);

    var tenantAttributes = new TenantAttributes();
    moduleCustomTenantService.afterTenantUpdate(tenantAttributes);

    verify(kafkaAdminService, org.mockito.Mockito.never()).createTopic(any());
  }

  @Test
  void afterTenantUpdate_withTenantCollection_shouldCreateSharedTopic() {
    var topicName = "mod-login-keycloak.logout";
    var numPartitions = 1;
    var replicationFactor = (short) 1;

    var kafkaTopic = new KafkaTopic();
    kafkaTopic.setName(topicName);
    kafkaTopic.setNumPartitions(numPartitions);
    kafkaTopic.setReplicationFactor(replicationFactor);

    when(context.getTenantId()).thenReturn("testtenant");
    when(kafkaProperties.getTenantTopics()).thenReturn(List.of(kafkaTopic));
    when(kafkaProperties.getProducerTenantCollection()).thenReturn(true);

    var tenantAttributes = new TenantAttributes();
    moduleCustomTenantService.afterTenantUpdate(tenantAttributes);

    var topicCaptor = ArgumentCaptor.forClass(NewTopic.class);
    verify(kafkaAdminService).createTopic(topicCaptor.capture());

    var createdTopic = topicCaptor.getValue();
    var expectedTopicName = String.format("folio.ALL.%s", topicName);
    assertThat(createdTopic.name()).isEqualTo(expectedTopicName);
  }
}
