package org.folio.login.util;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

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
}
