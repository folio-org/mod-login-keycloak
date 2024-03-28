package org.folio.login.integration.keycloak.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties
public class KeycloakAdminProperties {

  /**
   * Keycloak client id.
   */
  private String clientId;

  /**
   * Keycloak admin password.
   */
  private String password;

  /**
   * Keycloak admin username.
   */
  private String username;

  /**
   * Keycloak master realm.
   */
  private String realm;
}
