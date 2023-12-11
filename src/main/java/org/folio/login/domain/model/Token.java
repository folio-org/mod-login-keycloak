package org.folio.login.domain.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Token {

  /**
   * JWT.
   */
  private String jwt;

  /**
   * Token expiration age is seconds.
   */
  private Long expiresIn;

  /**
   * Token expiration date.
   */
  private String expirationDate;
}
