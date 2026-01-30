package org.folio.login.util;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.substringAfterLast;
import static org.keycloak.util.TokenUtil.getRefreshToken;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.folio.login.domain.model.KeycloakAuthentication;

@UtilityClass
public class JwtUtils {

  @SneakyThrows
  public static String extractTenant(String refreshToken) {
    var token = getRefreshToken(refreshToken);
    return substringAfterLast(token.getIssuer(), "/");
  }

  public static String tokenResponseAsString(KeycloakAuthentication tokenResponse) {
    return new ToStringBuilder(tokenResponse)
      .append("accessToken", tokenHash(tokenResponse.getAccessToken()))
      .append("refreshToken", tokenHash(tokenResponse.getRefreshToken()))
      .append("expiresIn", tokenResponse.getExpiresIn())
      .toString();
  }

  private static String tokenHash(String token) {
    return isNotEmpty(token) ? DigestUtils.sha256Hex(token) : null;
  }
}
