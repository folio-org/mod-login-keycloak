package org.folio.login.integration.keycloak.config;

import static jakarta.ws.rs.client.ClientBuilder.newBuilder;
import static org.apache.commons.lang3.StringUtils.stripToNull;
import static org.apache.http.conn.ssl.NoopHostnameVerifier.INSTANCE;
import static org.folio.common.utils.FeignClientTlsUtils.buildSslContext;

import lombok.extern.log4j.Log4j2;
import org.folio.common.configuration.properties.TlsProperties;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Log4j2
@Configuration
@EnableConfigurationProperties({KeycloakProperties.class})
public class KeycloakAdminConfiguration {

  @Bean
  public Keycloak keycloak(KeycloakProperties properties) {
    var admin = properties.getAdmin();

    var builder = KeycloakBuilder.builder()
      .realm("master")
      .serverUrl(properties.getUrl())
      .clientId(admin.getClientId())
      .username(stripToNull(admin.getUsername()))
      .password(stripToNull(admin.getPassword()))
      .grantType(OAuth2Constants.PASSWORD);

    var tls = properties.getTls();
    if (tls != null && tls.isEnabled()) {
      builder.resteasyClient(buildResteasyClient(tls));
    }
    return builder.build();
  }

  private static ResteasyClient buildResteasyClient(TlsProperties properties) {
    return (ResteasyClient) newBuilder().sslContext(buildSslContext(properties)).hostnameVerifier(INSTANCE).build();
  }
}
