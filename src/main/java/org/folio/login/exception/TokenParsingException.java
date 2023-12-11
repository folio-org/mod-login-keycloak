package org.folio.login.exception;

import lombok.Getter;

@Getter
public class TokenParsingException extends RuntimeException {
  public TokenParsingException(String message) {
    super(message);
  }
}
