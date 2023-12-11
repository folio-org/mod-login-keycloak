package org.folio.login.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class KeycloakAuthentication {

  /**
   * JWT Access Token.
   */
  @JsonProperty("access_token")
  private String accessToken;

  /**
   * JWT Refresh Token.
   */
  @JsonProperty("refresh_token")
  private String refreshToken;


  /**
   * Access token expiration age.
   */
  @JsonProperty("expires_in")
  private Long expiresIn;

  /**
   * Refresh token expiration age.
   */
  @JsonProperty("refresh_expires_in")
  private Long refreshExpiresIn;
}
