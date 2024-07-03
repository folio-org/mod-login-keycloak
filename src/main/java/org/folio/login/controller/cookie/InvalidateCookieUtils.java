package org.folio.login.controller.cookie;

import static org.apache.commons.lang3.StringUtils.EMPTY;

import jakarta.servlet.http.Cookie;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

@Log4j2
@UtilityClass
public class InvalidateCookieUtils {

  public static List<Cookie> formInvalidatedCookies(Cookie[] reqCookies, List<String> resCookies) {
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
