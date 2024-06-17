package org.folio.login.controller.cookie;

import static java.time.Instant.ofEpochSecond;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.collections4.ListUtils.union;
import static org.springframework.http.HttpHeaders.SET_COOKIE;

import jakarta.servlet.http.Cookie;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@Log4j2
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
    var servletRequest = (ServletServerHttpRequest) request;
    var servletResponse = (ServletServerHttpResponse) response;

    var onExceptionAnnotation = getInvalidateCookiesOnExceptionAnnotation(returnType);
    if (onExceptionAnnotation != null && !pathMatches(servletRequest, onExceptionAnnotation.paths())) {
      return body;
    }

    var reqCookies = servletRequest.getServletRequest().getCookies();
    var resCookies = servletResponse.getHeaders().getOrEmpty(SET_COOKIE);

    var invalidated = formInvalidatedCookies(reqCookies, resCookies);

    if (isNotEmpty(invalidated)) {
      var resulted = union(resCookies, invalidated);

      log.debug("Final list of response cookies: {}", resulted);
      servletResponse.getHeaders().put(SET_COOKIE, resulted);
    }

    return body;
  }

  private static ArrayList<String> formInvalidatedCookies(Cookie[] reqCookies, List<String> resCookies) {
    var safeReqCookies = safeArray(reqCookies);
    log.debug("Forming a list of request cookies to be invalidated: requestCookies = {}, "
        + "existingResponseCookies = {} ...",
      () -> Arrays.stream(safeReqCookies).map(InvalidateCookiesResponseBodyAdvice::cookieToString).toList(),
      () -> resCookies);

    var invalidated = new ArrayList<String>();
    for (var cookie : safeArray(reqCookies)) {
      if (cookieIsNotPresent(resCookies, cookie)) {
        invalidated.add(createInvalidatedCookie(cookie).toString());
      }
    }

    log.debug("Cookies to be invalidated: {}", invalidated);
    return invalidated;
  }

  private static String cookieToString(Cookie cookie) {
    return ReflectionToStringBuilder.toString(cookie);
  }

  private static boolean cookieIsNotPresent(List<String> resCookies, Cookie cookie) {
    return resCookies.stream().noneMatch(presentCookie -> presentCookie.startsWith(cookie.getName()));
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
    return ResponseCookie.from(cookie.getName())
      .maxAge(EXPIRED_DATE_IN_SECONDS)
      .build();
  }
}
