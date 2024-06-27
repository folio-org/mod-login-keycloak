package org.folio.login.controller.cookie;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.function.BiPredicate;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class UrlEqualsPredicate implements BiPredicate<HttpRequestResponseHolder, Exception> {

  private final String url;

  public static UrlEqualsPredicate urlEquals(String url) {
    if (isBlank(url)) {
      throw new IllegalArgumentException("Url is blank");
    }
    return new UrlEqualsPredicate(url);
  }

  @Override
  public boolean test(HttpRequestResponseHolder holder, Exception e) {
    return holder.request().getRequestURI().equalsIgnoreCase(url);
  }
}
