package org.folio.login.exception;

public class UnauthorizedException extends RuntimeException {

  /**
   * Creates {@link org.folio.login.exception.UnauthorizedException} with error message and error cause.
   *
   * @param message - error message as {@link String} object
   * @param cause - error cause as {@link Throwable} object
   */
  public UnauthorizedException(String message, Throwable cause) {
    super(message, cause);
  }
}
