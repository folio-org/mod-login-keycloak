package org.folio.login.integration.keycloak.config;

import org.folio.common.utils.tls.HttpClientTlsUtils;
import org.folio.login.integration.keycloak.KeycloakClient;
import org.folio.login.integration.keycloak.KeycloakUserClient;
import org.folio.login.integration.users.UsersClient;
import org.folio.login.integration.users.UsersKeycloakClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

/**
 * Keycloak and FOLIO HTTP service client configuration.
 */
@Configuration
public class KeycloakFeignConfiguration {

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

  @Bean
  UsersClient usersClient(HttpServiceProxyFactory factory) {
    return factory.createClient(UsersClient.class);
  }

  @Bean
  UsersKeycloakClient usersKeycloakClient(HttpServiceProxyFactory factory) {
    return factory.createClient(UsersKeycloakClient.class);
  }
}
