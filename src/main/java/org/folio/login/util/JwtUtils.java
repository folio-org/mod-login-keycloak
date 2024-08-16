package org.folio.login.util;

import static org.apache.commons.lang3.StringUtils.substringAfterLast;
import static org.keycloak.util.TokenUtil.getRefreshToken;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

@UtilityClass
public class JwtUtils {

  @SneakyThrows
  public static String extractTenant(String refreshToken) {
    var token = getRefreshToken(refreshToken);
    return substringAfterLast(token.getIssuer(), "/");
  }
}
