package org.folio.login.controller.cookie.filter;

import static org.folio.login.controller.cookie.InvalidateCookieUtils.invalidateCookies;
import static org.springframework.boot.web.servlet.filter.OrderedFilter.REQUEST_WRAPPER_FILTER_MAX_ORDER;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.function.BiPredicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.filter.OncePerRequestFilter;

@Log4j2
@RequiredArgsConstructor
public class InvalidateCookiesFilter extends OncePerRequestFilter {

  public static final int ORDER = REQUEST_WRAPPER_FILTER_MAX_ORDER - 1;

  private final BiPredicate<HttpRequestResponseHolder, Optional<Exception>> shouldInvalidateCookies;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
    throws ServletException, IOException {
    try {
      filterChain.doFilter(request, response);
    } catch (IOException | ServletException | RuntimeException e) {
      invalidateCookiesIfNeeded(request, response, e);
      throw e;
    }

    invalidateCookiesIfNeeded(request, response, null);
  }

  private void invalidateCookiesIfNeeded(HttpServletRequest request, HttpServletResponse response, Exception e) {
    if (!response.isCommitted()
      && shouldInvalidateCookies.test(new HttpRequestResponseHolder(request, response), Optional.ofNullable(e))) {
      invalidateCookies(request, response);
    }
  }
}
