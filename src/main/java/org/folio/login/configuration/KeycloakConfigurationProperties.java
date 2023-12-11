package org.folio.login.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "application.keycloak")
public class KeycloakConfigurationProperties {

  /**
   * Keycloak URL.
   */
  private String url;

  /**
   * Keycloak client suffix, e.g.: {@code -login-application}.
   */
  private String clientSuffix;

  @Data
  @Component
  @ConfigurationProperties(prefix = "application.keycloak.admin")
  public static class Admin {

    /**
     * Keycloak client id.
     * */
    private String clientId;

    /**
     * Keycloak admin password.
     * */
    private String password;

    /**
     * Keycloak admin username.
     * */
    private String username;

    /**
     * Keycloak master realm.
     * */
    private String realm;
  }
}
