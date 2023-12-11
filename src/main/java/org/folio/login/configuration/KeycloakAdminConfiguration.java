package org.folio.login.configuration;

import static org.apache.commons.lang3.StringUtils.stripToNull;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeycloakAdminConfiguration {
  @Bean
  public Keycloak keycloak(KeycloakConfigurationProperties properties,
    KeycloakConfigurationProperties.Admin admin) {
    return KeycloakBuilder.builder()
      .realm("master")
      .serverUrl(properties.getUrl())
      .clientId(admin.getClientId())
      .username(stripToNull(admin.getUsername()))
      .password(stripToNull(admin.getPassword()))
      .grantType(OAuth2Constants.PASSWORD)
      .build();
  }
}
