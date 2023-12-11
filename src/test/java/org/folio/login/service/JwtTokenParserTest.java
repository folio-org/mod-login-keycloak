package org.folio.login.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.folio.login.exception.TokenParsingException;
import org.folio.test.types.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class JwtTokenParserTest {

  private JwtTokenParser service = new JwtTokenParser(new ObjectMapper());

  @Test
  void parseExpirationDate_positive() {
    var payload = Base64.getEncoder().encodeToString("{\"exp\":1693241071}".getBytes(StandardCharsets.UTF_8));
    var token = String.format("header.%s.sig", payload);
    var actual = service.parseExpirationDate(token);
    assertThat(actual).isEqualTo("2023-08-28T16:44:31Z");
  }

  @Test
  void parseExpirationDate_negative_nullToken() {
    assertThatThrownBy(() -> service.parseExpirationDate(null))
      .isInstanceOf(TokenParsingException.class)
      .hasMessage("Failed to find auth token in request.");
  }

  @Test
  void parseExpirationDate_negative_invalidAmountOfSegments() {
    var token = "abc";
    assertThatThrownBy(() -> service.parseExpirationDate(token))
      .isInstanceOf(TokenParsingException.class)
      .hasMessage("Invalid amount of segments in JWT token.");
  }

  @Test
  void parseExpirationDate_negative_invalidToken() {
    var payload = Base64.getEncoder().encodeToString("{}".getBytes(StandardCharsets.UTF_8));
    var token = String.format("header.%s.sig", payload);
    assertThatThrownBy(() -> service.parseExpirationDate(token))
      .isInstanceOf(TokenParsingException.class)
      .hasMessage("Invalid token.");
  }
}
