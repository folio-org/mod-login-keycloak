package org.folio.login.configuration;

import org.folio.login.integration.users.UsersClient;
import org.folio.login.integration.users.UsersKeycloakClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class ClientConfig {

  @Bean
  UsersClient usersClient(HttpServiceProxyFactory factory) {
    return factory.createClient(UsersClient.class);
  }

  @Bean
  UsersKeycloakClient usersKeycloakClient(HttpServiceProxyFactory factory) {
    return factory.createClient(UsersKeycloakClient.class);
  }
}
