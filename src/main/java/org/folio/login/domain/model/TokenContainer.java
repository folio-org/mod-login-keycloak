package org.folio.login.domain.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TokenContainer {

  private Token accessToken;
  private Token refreshToken;

  public String getAccessTokenExpirationDate() {
    if (accessToken == null) {
      return null;
    }
    return accessToken.getExpirationDate();
  }

  public String getRefreshTokenExpirationDate() {
    if (refreshToken == null) {
      return null;
    }
    return refreshToken.getExpirationDate();
  }
}
