package org.folio.login.exception;

import lombok.Getter;

@Getter
public class TokenLogoutException extends RuntimeException {
  public TokenLogoutException(String message, Throwable cause) {
    super(message, cause);
  }
}
