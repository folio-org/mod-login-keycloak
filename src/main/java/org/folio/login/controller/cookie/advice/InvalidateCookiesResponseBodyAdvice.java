package org.folio.login.controller.cookie.advice;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.folio.login.controller.cookie.InvalidateCookieUtils.formInvalidatedCookies;
import static org.springframework.http.HttpHeaders.SET_COOKIE;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.MediaType;
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

  @Override
  public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
    return getInvalidateCookiesAnnotation(returnType) != null
      || getInvalidateCookiesOnExceptionAnnotation(returnType) != null;
  }

  @Override
  public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
    Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request,
    ServerHttpResponse response) {
    var httpRequest = ((ServletServerHttpRequest) request).getServletRequest();
    var httpResponse = ((ServletServerHttpResponse) response).getServletResponse();

    var onExceptionAnnotation = getInvalidateCookiesOnExceptionAnnotation(returnType);
    if (onExceptionAnnotation != null && !pathMatches(httpRequest, onExceptionAnnotation.paths())) {
      return body;
    }

    var reqCookies = httpRequest.getCookies();
    var resCookies = new ArrayList<>(httpResponse.getHeaders(SET_COOKIE));

    var invalidated = formInvalidatedCookies(reqCookies, resCookies);

    if (isNotEmpty(invalidated)) {
      invalidated.forEach(httpResponse::addCookie);

      var resulted = httpResponse.getHeaders(SET_COOKIE);
      log.debug("Final list of response cookies: {}", resulted);
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

  private static boolean pathMatches(HttpServletRequest request, String[] path) {
    return ArrayUtils.isEmpty(path) || ArrayUtils.contains(path, request.getRequestURI());
  }
}
