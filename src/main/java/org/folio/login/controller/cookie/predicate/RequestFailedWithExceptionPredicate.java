package org.folio.login.controller.cookie.predicate;

import java.util.function.BiPredicate;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.folio.login.controller.cookie.HttpRequestResponseHolder;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RequestFailedWithExceptionPredicate implements BiPredicate<HttpRequestResponseHolder, Exception> {

  private static final RequestFailedWithExceptionPredicate INSTANCE = new RequestFailedWithExceptionPredicate();

  public static RequestFailedWithExceptionPredicate failedWithException() {
    return INSTANCE;
  }

  @Override
  public boolean test(HttpRequestResponseHolder holder, Exception e) {
    return e != null;
  }
}
