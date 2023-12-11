package org.folio.login.exception;

import lombok.Getter;

@Getter
public class TokenRefreshException extends RuntimeException {
  public TokenRefreshException(String message, Throwable cause) {
    super(message, cause);
  }
}
