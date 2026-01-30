package org.folio.login.configuration.property;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@Configuration
@ConfigurationProperties(prefix = "application.admin-token-cache")
public class TokenCacheProperties {

  /**
   * Specifies the amount of seconds for a cache entry invalidation prior to the token expiration.
   * The purpose of early cache entry expiration is to minimize a risk that a token expires
   * when a request is being processed.
   */
  @NotNull
  @Positive
  private Integer refreshBeforeExpirySec;
}
