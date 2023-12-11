package org.folio.login.configuration.property;

import static org.folio.login.configuration.property.CookieProperties.SameSite.NONE;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Setter
@Configuration
@ConfigurationProperties(prefix = "application.cookies")
public class CookieProperties {

  private SameSite sameSite;

  public String getSameSiteValue() {
    if (sameSite == null) {
      return NONE.getValue();
    }
    return sameSite.getValue();
  }

  @RequiredArgsConstructor
  public enum SameSite {
    NONE("None"),
    LAX("Lax"),
    STRICT("Strict");

    @Getter
    private final String value;
  }
}
