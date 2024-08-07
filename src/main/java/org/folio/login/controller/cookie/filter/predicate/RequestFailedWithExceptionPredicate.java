package org.folio.login.controller.cookie.filter.predicate;

import java.util.Optional;
import java.util.function.BiPredicate;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.folio.login.controller.cookie.filter.HttpRequestResponseHolder;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RequestFailedWithExceptionPredicate
  implements BiPredicate<HttpRequestResponseHolder, Optional<Exception>> {

  private static final RequestFailedWithExceptionPredicate INSTANCE = new RequestFailedWithExceptionPredicate();

  public static RequestFailedWithExceptionPredicate failedWithException() {
    return INSTANCE;
  }

  @Override
  public boolean test(HttpRequestResponseHolder holder, Optional<Exception> e) {
    return e.isPresent();
  }
}
