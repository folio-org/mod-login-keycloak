package org.folio.login.controller;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.apache.logging.log4j.Level.DEBUG;
import static org.apache.logging.log4j.Level.WARN;
import static org.folio.login.domain.dto.ErrorCode.FOUND_ERROR;
import static org.folio.login.domain.dto.ErrorCode.NOT_FOUND_ERROR;
import static org.folio.login.domain.dto.ErrorCode.SERVICE_ERROR;
import static org.folio.login.domain.dto.ErrorCode.TOKEN_LOGOUT_UNPROCESSABLE;
import static org.folio.login.domain.dto.ErrorCode.TOKEN_PARSE_FAILURE;
import static org.folio.login.domain.dto.ErrorCode.TOKEN_REFRESH_UNPROCESSABLE;
import static org.folio.login.domain.dto.ErrorCode.UNKNOWN_ERROR;
import static org.folio.login.domain.dto.ErrorCode.VALIDATION_ERROR;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.BadRequestException;
import java.util.List;
import java.util.Optional;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.Level;
import org.folio.cql2pgjson.exception.CQLFeatureUnsupportedException;
import org.folio.login.controller.cookie.advice.InvalidateCookiesOnException;
import org.folio.login.domain.dto.Error;
import org.folio.login.domain.dto.ErrorCode;
import org.folio.login.domain.dto.ErrorResponse;
import org.folio.login.domain.dto.Parameter;
import org.folio.login.exception.RequestValidationException;
import org.folio.login.exception.ServiceException;
import org.folio.login.exception.TokenLogoutException;
import org.folio.login.exception.TokenParsingException;
import org.folio.login.exception.TokenRefreshException;
import org.folio.spring.cql.CqlQueryValidationException;
import org.folio.spring.exception.NotFoundException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Log4j2
@RestControllerAdvice
@InvalidateCookiesOnException(paths = {
  "/authn/token",
  "/authn/logout",
  "/authn/logout-all"
})
public class ApiExceptionHandler {

  /**
   * Catches and handles all exceptions for type {@link UnsupportedOperationException}.
   *
   * @param exception {@link UnsupportedOperationException} to process
   * @return {@link ResponseEntity} with {@link ErrorResponse} body
   */
  @ExceptionHandler(UnsupportedOperationException.class)
  public ResponseEntity<ErrorResponse> handleUnsupportedOperationException(UnsupportedOperationException exception) {
    logException(DEBUG, exception);
    return buildResponseEntity(exception, BAD_REQUEST, SERVICE_ERROR);
  }

  /**
   * Catches and handles all exceptions for type {@link MethodArgumentNotValidException}.
   *
   * @param exception {@link MethodArgumentNotValidException} to process
   * @return {@link ResponseEntity} with {@link ErrorResponse} body
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
    MethodArgumentNotValidException exception) {
    var validationErrors = Optional.of(exception.getBindingResult()).map(Errors::getAllErrors).orElse(emptyList());
    var errorResponse = new ErrorResponse();
    validationErrors.forEach(error ->
      errorResponse.addErrorsItem(new Error()
        .message(error.getDefaultMessage())
        .code(ErrorCode.VALIDATION_ERROR)
        .type(MethodArgumentNotValidException.class.getSimpleName())
        .addParametersItem(new Parameter()
          .key(((FieldError) error).getField())
          .value(String.valueOf(((FieldError) error).getRejectedValue())))));
    errorResponse.totalRecords(errorResponse.getErrors().size());

    return buildResponseEntity(errorResponse, BAD_REQUEST);
  }

  /**
   * Catches and handles all exceptions for type {@link ConstraintViolationException}.
   *
   * @param exception {@link ConstraintViolationException} to process
   * @return {@link ResponseEntity} with {@link ErrorResponse} body
   */
  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException exception) {
    logException(DEBUG, exception);
    var errorResponse = new ErrorResponse();
    exception.getConstraintViolations().forEach(constraintViolation ->
      errorResponse.addErrorsItem(new Error()
        .message(String.format("%s %s", constraintViolation.getPropertyPath(), constraintViolation.getMessage()))
        .code(VALIDATION_ERROR)
        .type(ConstraintViolationException.class.getSimpleName())));
    errorResponse.totalRecords(errorResponse.getErrors().size());

    return buildResponseEntity(errorResponse, BAD_REQUEST);
  }

  /**
   * Catches and handles all exceptions for type {@link RequestValidationException}.
   *
   * @param exception {@link RequestValidationException} to process
   * @return {@link ResponseEntity} with {@link ErrorResponse} body
   */
  @ExceptionHandler(RequestValidationException.class)
  public ResponseEntity<ErrorResponse> handleRequestValidationException(RequestValidationException exception) {
    var errorResponse = buildValidationError(exception,
      List.of(new Parameter().key(exception.getKey()).value(exception.getValue())));
    return buildResponseEntity(errorResponse, BAD_REQUEST);
  }

  /**
   * Catches and handles common request validation exceptions.
   *
   * @param exception {@link Exception} object to process
   * @return {@link ResponseEntity} with {@link ErrorResponse} body
   */
  @ExceptionHandler({
    IllegalArgumentException.class,
    CqlQueryValidationException.class,
    MissingRequestHeaderException.class,
    CQLFeatureUnsupportedException.class,
    InvalidDataAccessApiUsageException.class,
    HttpMediaTypeNotSupportedException.class,
    MethodArgumentTypeMismatchException.class,
    MissingRequestCookieException.class
  })
  public ResponseEntity<ErrorResponse> handleValidationExceptions(Exception exception) {
    logException(DEBUG, exception);
    return buildResponseEntity(exception, BAD_REQUEST, VALIDATION_ERROR);
  }

  /**
   * Catches and handles all exceptions for type {@link EntityExistsException}.
   *
   * @param exception {@link EntityExistsException} object
   * @return {@link ResponseEntity} with {@link ErrorResponse} body.
   */
  @ExceptionHandler(EntityExistsException.class)
  public ResponseEntity<ErrorResponse> handleEntityExistsException(EntityExistsException exception) {
    logException(DEBUG, exception);
    return buildResponseEntity(exception, BAD_REQUEST, FOUND_ERROR);
  }

  /**
   * Catches and handles common request service exceptions.
   *
   * @param exception {@link Exception} object to process
   * @return {@link ResponseEntity} with {@link ErrorResponse} body
   */
  @ExceptionHandler({IllegalStateException.class})
  public ResponseEntity<ErrorResponse> handleServiceLevelExceptions(Exception exception) {
    logException(DEBUG, exception);
    return buildResponseEntity(exception, BAD_REQUEST, SERVICE_ERROR);
  }

  /**
   * Catches and handles all exceptions for error code {@link ErrorCode#NOT_FOUND_ERROR}.
   *
   * @param exception {@link Exception} object
   * @return {@link ResponseEntity} with {@link ErrorResponse} body.
   */
  @ExceptionHandler({EntityNotFoundException.class, NotFoundException.class})
  public ResponseEntity<ErrorResponse> handleNotFoundException(Exception exception) {
    logException(DEBUG, exception);
    return buildResponseEntity(exception, NOT_FOUND, NOT_FOUND_ERROR);
  }

  /**
   * Catches and handles all exceptions for type {@link HttpMessageNotReadableException}.
   *
   * @param exception {@link HttpMessageNotReadableException} object
   * @return {@link ResponseEntity} with {@link ErrorResponse} body.
   */
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> handlerHttpMessageNotReadableException(
    HttpMessageNotReadableException exception) {

    return Optional.ofNullable(exception.getCause())
      .map(Throwable::getCause)
      .filter(IllegalArgumentException.class::isInstance)
      .map(IllegalArgumentException.class::cast)
      .map(this::handleValidationExceptions)
      .orElseGet(() -> {
        logException(DEBUG, exception);
        return buildResponseEntity(exception, BAD_REQUEST, VALIDATION_ERROR);
      });
  }

  /**
   * Catches and handles all exceptions for type {@link org.folio.login.exception.TokenParsingException}.
   *
   * @param exception object
   * @return {@link ResponseEntity} with {@link ErrorResponse} body.
   */
  @ExceptionHandler(TokenParsingException.class)
  public ResponseEntity<ErrorResponse> handleTokenParsingException(Exception exception) {
    logException(WARN, exception);
    return buildResponseEntity(exception, BAD_REQUEST, TOKEN_PARSE_FAILURE);
  }

  /**
   * Catches and handles all exceptions for type {@link TokenLogoutException}.
   *
   * @param exception {@link TokenLogoutException} object
   * @return {@link ResponseEntity} with {@link ErrorResponse} body.
   */
  @ExceptionHandler(TokenLogoutException.class)
  public ResponseEntity<ErrorResponse> handleTokenLogoutException(TokenLogoutException exception) {
    logException(WARN, exception);
    return buildResponseEntity(exception, UNPROCESSABLE_ENTITY, TOKEN_LOGOUT_UNPROCESSABLE);
  }

  /**
   * Catches and handles all exceptions for type {@link TokenRefreshException}.
   *
   * @param exception {@link TokenRefreshException} object
   * @return {@link ResponseEntity} with {@link ErrorResponse} body.
   */
  @ExceptionHandler(TokenRefreshException.class)
  public ResponseEntity<ErrorResponse> handleTokenRefreshException(TokenRefreshException exception) {
    logException(WARN, exception);
    return buildResponseEntity(exception, UNPROCESSABLE_ENTITY, TOKEN_REFRESH_UNPROCESSABLE);
  }

  /**
   * Catches and handles all exceptions for type {@link BadRequestException}.
   *
   * @param exception {@link BadRequestException} object
   * @return {@link ResponseEntity} with {@link ErrorResponse} body.
   */
  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<ErrorResponse> handleBadRequestException(BadRequestException exception) {
    logException(WARN, exception);
    return buildResponseEntity(exception, BAD_REQUEST, VALIDATION_ERROR);
  }

  /**
   * Catches and handles all exceptions for type {@link org.folio.login.exception.ServiceException}.
   *
   * @param exception {@link org.folio.login.exception.ServiceException} object
   * @return {@link ResponseEntity} with {@link ErrorResponse} body.
   */
  @ExceptionHandler(ServiceException.class)
  public ResponseEntity<ErrorResponse> handleServiceException(Exception exception) {
    logException(DEBUG, exception);
    var errorParameters = singletonList(new Parameter().key("cause").value(exception.getCause().getMessage()));
    var errorResponse = buildErrorResponse(exception, errorParameters, SERVICE_ERROR);
    return buildResponseEntity(errorResponse, BAD_REQUEST);
  }

  /**
   * Catches and handles all exceptions for type {@link MissingServletRequestParameterException}.
   *
   * @param exception {@link MissingServletRequestParameterException} to process
   * @return {@link ResponseEntity} with {@link ErrorResponse} body
   */
  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(
    MissingServletRequestParameterException exception) {
    logException(DEBUG, exception);
    return buildResponseEntity(exception, BAD_REQUEST, VALIDATION_ERROR);
  }

  /**
   * Handles all uncaught exceptions.
   *
   * @param exception {@link Exception} object
   * @return {@link ResponseEntity} with {@link ErrorResponse} body.
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleAllOtherExceptions(Exception exception) {
    logException(WARN, exception);
    return buildResponseEntity(exception, INTERNAL_SERVER_ERROR, UNKNOWN_ERROR);
  }

  private static ErrorResponse buildValidationError(Exception exception, List<Parameter> parameters) {
    return buildErrorResponse(exception, parameters, VALIDATION_ERROR);
  }

  private static ErrorResponse buildErrorResponse(Exception exception, List<Parameter> parameters, ErrorCode code) {
    var error = new Error()
      .type(exception.getClass().getSimpleName())
      .code(code)
      .message(exception.getMessage())
      .parameters(isNotEmpty(parameters) ? parameters : null);
    return new ErrorResponse().errors(List.of(error)).totalRecords(1);
  }

  private static ResponseEntity<ErrorResponse> buildResponseEntity(
    Exception exception, HttpStatus status, ErrorCode code) {

    var errorResponse = new ErrorResponse()
      .errors(List.of(new Error()
        .message(exception.getMessage())
        .type(exception.getClass().getSimpleName())
        .code(code)))
      .totalRecords(1);

    return buildResponseEntity(errorResponse, status);
  }

  private static ResponseEntity<ErrorResponse> buildResponseEntity(ErrorResponse errorResponse, HttpStatus status) {
    return ResponseEntity.status(status).body(errorResponse);
  }

  private static void logException(Level level, Exception exception) {
    log.log(level, "Handling exception", exception);
  }
}
