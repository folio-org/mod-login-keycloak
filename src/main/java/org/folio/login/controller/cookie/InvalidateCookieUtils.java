package org.folio.login.controller.cookie;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.collections4.ListUtils.union;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.springframework.http.HttpHeaders.SET_COOKIE;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;

@Log4j2
@UtilityClass
public class InvalidateCookieUtils {

  public static void invalidateCookies(HttpServletRequest request, HttpServletResponse response) {
    var reqCookies = request.getCookies();
    var resCookies = new ArrayList<>(response.getHeaders(SET_COOKIE));

    var invalidated = formInvalidatedCookies(reqCookies, resCookies);

    if (isNotEmpty(invalidated)) {
      invalidated.forEach(response::addCookie);

      var resulted = response.getHeaders(SET_COOKIE);
      log.debug("Final list of response cookies: {}", resulted);
    }
  }

  public static void invalidateCookies(ServletServerHttpRequest request, ServletServerHttpResponse response) {
    var reqCookies = request.getServletRequest().getCookies();
    var resCookies = response.getHeaders().getOrEmpty(SET_COOKIE);

    var invalidated = formInvalidatedCookies(reqCookies, resCookies);

    if (isNotEmpty(invalidated)) {
      var resulted = union(resCookies,
        invalidated.stream()
          .map(InvalidateCookieUtils::toResponseCookie)
          .map(ResponseCookie::toString).toList());

      log.debug("Final list of response cookies: {}", resulted);
      response.getHeaders().put(SET_COOKIE, resulted);
    }
  }

  private static ResponseCookie toResponseCookie(Cookie cookie) {
    return ResponseCookie.from(cookie.getName(), defaultString(cookie.getValue()))
      .httpOnly(cookie.isHttpOnly())
      .secure(cookie.getSecure())
      .path(cookie.getPath())
      .maxAge(cookie.getMaxAge())
      .domain(cookie.getDomain())
      .path(cookie.getPath())
      .build();
  }

  private static List<Cookie> formInvalidatedCookies(Cookie[] reqCookies, List<String> resCookies) {
    var safeReqCookies = safeArray(reqCookies);
    log.debug("Forming a list of request cookies to be invalidated: requestCookies = {}, "
        + "existingResponseCookies = {} ...",
      () -> cookiesToString(Arrays.stream(safeReqCookies)),
      () -> resCookies);

    var invalidated = new ArrayList<Cookie>();
    for (var cookie : safeReqCookies) {
      if (cookieIsNotPresent(resCookies, cookie)) {
        invalidated.add(createInvalidatedCookie(cookie));
      }
    }

    log.debug("Cookies to be invalidated: {}", () -> cookiesToString(invalidated.stream()));
    return invalidated;
  }

  private static List<String> cookiesToString(Stream<Cookie> cookieStream) {
    return cookieStream.map(InvalidateCookieUtils::cookieToString).toList();
  }

  private static Cookie[] safeArray(Cookie[] cookies) {
    return ArrayUtils.nullToEmpty(cookies, Cookie[].class);
  }

  private static String cookieToString(Cookie cookie) {
    return ReflectionToStringBuilder.toString(cookie);
  }

  private static boolean cookieIsNotPresent(List<String> resCookies, Cookie cookie) {
    return resCookies.stream().noneMatch(presentCookie -> presentCookie.startsWith(cookie.getName()));
  }

  private static Cookie createInvalidatedCookie(Cookie cookie) {
    var invalidated = new Cookie(cookie.getName(), EMPTY);
    invalidated.setSecure(cookie.getSecure());
    invalidated.setHttpOnly(cookie.isHttpOnly());
    invalidated.setMaxAge(0);
    invalidated.setDomain(cookie.getDomain());
    invalidated.setPath(cookie.getPath());

    return invalidated;
  }
}
