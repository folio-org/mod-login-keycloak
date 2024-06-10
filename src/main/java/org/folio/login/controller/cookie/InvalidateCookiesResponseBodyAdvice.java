package org.folio.login.controller.cookie;

import static java.time.Instant.ofEpochSecond;
import static org.springframework.http.HttpHeaders.SET_COOKIE;

import jakarta.servlet.http.Cookie;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@ControllerAdvice
public final class InvalidateCookiesResponseBodyAdvice implements ResponseBodyAdvice<Object> {

  private static final long EXPIRED_DATE_IN_SECONDS = ofEpochSecond(0).toEpochMilli() * 1000;

  @Override
  public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
    return getInvalidateCookiesAnnotation(returnType) != null
      || getInvalidateCookiesOnExceptionAnnotation(returnType) != null;
  }

  @Override
  public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
    Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request,
    ServerHttpResponse response) {
    ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;

    var onExceptionAnnotation = getInvalidateCookiesOnExceptionAnnotation(returnType);
    if (onExceptionAnnotation != null && !pathMatches(servletRequest, onExceptionAnnotation.paths())) {
      return body;
    }

    var cookies = servletRequest.getServletRequest().getCookies();
    for (var cookie: safeArray(cookies)) {
      response.getHeaders()
        .add(SET_COOKIE, createInvalidatedCookie(cookie).toString());
    }

    return body;
  }

  private static InvalidateCookies getInvalidateCookiesAnnotation(MethodParameter returnType) {
    return returnType.getMethodAnnotation(InvalidateCookies.class);
  }

  private static InvalidateCookiesOnException getInvalidateCookiesOnExceptionAnnotation(MethodParameter returnType) {
    var result = returnType.getMethodAnnotation(InvalidateCookiesOnException.class);
    if (result == null) {
      result = AnnotationUtils.findAnnotation(returnType.getContainingClass(), InvalidateCookiesOnException.class);
    }
    return result;
  }

  private static boolean pathMatches(ServletServerHttpRequest servletRequest, String[] path) {
    return ArrayUtils.isEmpty(path) || ArrayUtils.contains(path, servletRequest.getServletRequest().getPathInfo());
  }

  private static Cookie[] safeArray(Cookie[] cookies) {
    return ArrayUtils.nullToEmpty(cookies, Cookie[].class);
  }

  private static ResponseCookie createInvalidatedCookie(Cookie cookie) {
    return ResponseCookie.from(cookie.getName(), cookie.getValue())
      .maxAge(EXPIRED_DATE_IN_SECONDS)
      .build();
  }
}
