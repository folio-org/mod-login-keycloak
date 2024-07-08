package org.folio.login.controller.cookie.filter.predicate;

import java.util.Optional;
import java.util.function.BiPredicate;
import lombok.RequiredArgsConstructor;
import org.folio.login.controller.cookie.filter.HttpRequestResponseHolder;
import org.springframework.http.HttpMethod;

@RequiredArgsConstructor
public final class RequestHttpMethodPredicate implements BiPredicate<HttpRequestResponseHolder, Optional<Exception>> {

  private final HttpMethod httpMethod;

  @Override
  public boolean test(HttpRequestResponseHolder holder, Optional<Exception> e) {
    return holder.request().getMethod().equalsIgnoreCase(httpMethod.name());
  }
}
