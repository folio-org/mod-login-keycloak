package org.folio.login.controller.cookie.predicate;

import java.util.function.BiPredicate;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.folio.login.controller.cookie.HttpRequestResponseHolder;
import org.springframework.http.HttpStatus;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RequestFailedWithErrorPredicate implements BiPredicate<HttpRequestResponseHolder, Exception> {

  private static final RequestFailedWithErrorPredicate INSTANCE = new RequestFailedWithErrorPredicate();

  public static RequestFailedWithErrorPredicate failedWithError() {
    return INSTANCE;
  }

  @Override
  public boolean test(HttpRequestResponseHolder holder, Exception e) {
    return HttpStatus.valueOf(holder.response().getStatus()).isError();
  }
}
