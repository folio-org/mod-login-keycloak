package org.folio.login.controller.cookie;

import java.util.function.BiPredicate;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

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
