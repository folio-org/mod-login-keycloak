package org.folio.login.integration.keycloak.config;

import lombok.Data;
import org.folio.common.configuration.properties.TlsProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "application.keycloak")
public class KeycloakProperties {

  /**
   * Keycloak URL.
   */
  private String url;

  /**
   * Keycloak client suffix, e.g.: {@code -login-application}.
   */
  private String clientSuffix;

  @NestedConfigurationProperty
  private TlsProperties tls;
  @NestedConfigurationProperty
  private KeycloakAdminProperties admin;
}
