package org.folio.login.integration.keycloak.config;

import org.folio.common.utils.tls.HttpClientTlsUtils;
import org.folio.login.integration.keycloak.KeycloakClient;
import org.folio.login.integration.keycloak.KeycloakUserClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class KeycloakClientConfiguration {

  @Bean
  KeycloakClient keycloakClient(KeycloakProperties properties) {
    return HttpClientTlsUtils.buildHttpServiceClient(
      RestClient.builder(), properties.getTls(), properties.getUrl(), KeycloakClient.class);
  }

  @Bean
  KeycloakUserClient keycloakUserClient(KeycloakProperties properties) {
    return HttpClientTlsUtils.buildHttpServiceClient(
      RestClient.builder(), properties.getTls(), properties.getUrl(), KeycloakUserClient.class);
  }
}
