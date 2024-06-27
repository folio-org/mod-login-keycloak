package org.folio.login.controller.cookie;

import java.util.function.BiPredicate;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;

@RequiredArgsConstructor
public final class RequestHttpMethodPredicate implements BiPredicate<HttpRequestResponseHolder, Exception> {

  private final HttpMethod httpMethod;

  @Override
  public boolean test(HttpRequestResponseHolder holder, Exception e) {
    return holder.request().getMethod().equalsIgnoreCase(httpMethod.name());
  }
}
