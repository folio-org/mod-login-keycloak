package org.folio.login.controller.cookie;

import static org.apache.commons.lang3.StringUtils.isBlank;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.function.BiPredicate;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class UrlEqualsPredicate implements BiPredicate<HttpServletRequest, HttpServletResponse> {

  private final String url;

  public static UrlEqualsPredicate urlEquals(String url) {
    if (isBlank(url)) {
      throw new IllegalArgumentException("Url is blank");
    }
    return new UrlEqualsPredicate(url);
  }

  @Override
  public boolean test(HttpServletRequest request, HttpServletResponse response) {
    return request.getRequestURI().equalsIgnoreCase(url);
  }
}
