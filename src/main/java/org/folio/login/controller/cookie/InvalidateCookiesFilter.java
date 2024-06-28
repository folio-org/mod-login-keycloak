package org.folio.login.controller.cookie;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.springframework.boot.web.servlet.filter.OrderedFilter.REQUEST_WRAPPER_FILTER_MAX_ORDER;
import static org.springframework.http.HttpHeaders.SET_COOKIE;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.springframework.web.filter.OncePerRequestFilter;

@Log4j2
@RequiredArgsConstructor
public class InvalidateCookiesFilter extends OncePerRequestFilter {

  public static final int ORDER = REQUEST_WRAPPER_FILTER_MAX_ORDER - 1;

  private final BiPredicate<HttpRequestResponseHolder, Exception> shouldInvalidateCookies;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
    throws ServletException, IOException {
    try {
      filterChain.doFilter(request, response);

      invalidateCookiesIfNeeded(request, response, null);
    } catch (IOException | ServletException | RuntimeException e) {
      invalidateCookiesIfNeeded(request, response, e);
      throw e;
    }
  }

  private void invalidateCookiesIfNeeded(HttpServletRequest request, HttpServletResponse response, Exception e) {
    if (shouldInvalidateCookies.test(new HttpRequestResponseHolder(request, response), e)) {
      invalidateCookies(request, response);
    }
  }

  private static void invalidateCookies(HttpServletRequest request, HttpServletResponse response) {
    var reqCookies = request.getCookies();
    var resCookies = new ArrayList<>(response.getHeaders(SET_COOKIE));

    var invalidated = formInvalidatedCookies(reqCookies, resCookies);

    if (isNotEmpty(invalidated)) {
      invalidated.forEach(response::addCookie);

      var resulted = response.getHeaders(SET_COOKIE);
      log.debug("Final list of response cookies: {}", resulted);
    }
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
    return cookieStream.map(InvalidateCookiesFilter::cookieToString).toList();
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
    invalidated.setSecure(true);
    invalidated.setHttpOnly(true);
    invalidated.setMaxAge(0);

    return invalidated;
  }
}
