package org.folio.login.util;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.folio.login.util.JwtUtils.tokenResponseAsString;

import org.apache.commons.codec.digest.DigestUtils;
import org.assertj.core.api.Assertions;
import org.folio.login.domain.model.KeycloakAuthentication;
import org.folio.test.security.TestJwtGenerator;
import org.folio.test.types.UnitTest;
import org.junit.jupiter.api.Test;

@UnitTest
class JwtUtilsTest {

  @Test
  void extractTenant_positive() {
    var tenantId = "testtenant";
    var testToken = TestJwtGenerator.generateJwtToken("http://localhost:8081", tenantId);

    var extractedTenant = JwtUtils.extractTenant(testToken);

    assertThat(extractedTenant).isEqualTo(tenantId);
  }

  @Test
  void tokenResponseAsString_positive() {
    var tokenResponse = new KeycloakAuthentication();
    tokenResponse.setAccessToken("validAccessToken");
    tokenResponse.setRefreshToken("validRefreshToken");
    tokenResponse.setExpiresIn(100L);

    var result = tokenResponseAsString(tokenResponse);

    Assertions.assertThat(result)
      .contains("accessToken=" + DigestUtils.sha256Hex("validAccessToken"))
      .contains("refreshToken=" + DigestUtils.sha256Hex("validRefreshToken"))
      .contains("expiresIn=100");
  }

  @Test
  void tokenResponseAsString_positive_tokenAndRefreshTokenAreNull() {
    var tokenResponse = new KeycloakAuthentication();
    tokenResponse.setAccessToken(null);
    tokenResponse.setRefreshToken(null);
    tokenResponse.setExpiresIn(100L);

    var result = tokenResponseAsString(tokenResponse);

    Assertions.assertThat(result).contains("accessToken=<null>")
      .contains("refreshToken=<null>")
      .contains("expiresIn=100");
  }
}
