package org.folio.login;

import org.folio.common.configuration.properties.FolioEnvironment;
import org.folio.integration.kafka.EnableKafka;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;

@EnableKafka
@EnableCaching
@EnableFeignClients
@SpringBootApplication
@Import(FolioEnvironment.class)
public class LoginKeycloakApplication {

  /**
   * Runs spring application.
   *
   * @param args command line arguments.
   */
  public static void main(String[] args) {
    SpringApplication.run(LoginKeycloakApplication.class, args);
  }
}
