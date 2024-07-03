package org.folio.login.controller.cookie.filter;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.folio.login.controller.cookie.InvalidateCookieUtils.formInvalidatedCookies;
import static org.springframework.boot.web.servlet.filter.OrderedFilter.REQUEST_WRAPPER_FILTER_MAX_ORDER;
import static org.springframework.http.HttpHeaders.SET_COOKIE;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.function.BiPredicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
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
    } catch (IOException | ServletException | RuntimeException e) {
      invalidateCookiesIfNeeded(request, response, e);
      throw e;
    }
  }

  private void invalidateCookiesIfNeeded(HttpServletRequest request, HttpServletResponse response, Exception e) {
    if (!response.isCommitted() && shouldInvalidateCookies.test(new HttpRequestResponseHolder(request, response), e)) {
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
}
