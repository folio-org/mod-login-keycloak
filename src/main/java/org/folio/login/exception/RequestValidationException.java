package org.folio.login.exception;

import static org.folio.login.domain.dto.ErrorCode.VALIDATION_ERROR;

import lombok.Getter;
import org.folio.login.domain.dto.ErrorCode;

@Getter
public class RequestValidationException extends RuntimeException {

  private final String key;
  private final String value;
  private final ErrorCode errorCode;

  /**
   * Creates {@link RequestValidationException} object for given message, key and value.
   *
   * @param message - validation error message
   * @param key - validation key as field or parameter name
   * @param value - invalid parameter value
   */
  public RequestValidationException(String message, String key, String value) {
    super(message);

    this.key = key;
    this.value = value;
    this.errorCode = VALIDATION_ERROR;
  }
}
