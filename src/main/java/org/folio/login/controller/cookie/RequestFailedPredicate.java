package org.folio.login.controller.cookie;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.function.BiPredicate;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RequestFailedPredicate implements BiPredicate<HttpServletRequest, HttpServletResponse> {

  private static final RequestFailedPredicate INSTANCE = new RequestFailedPredicate();

  public static RequestFailedPredicate requestFailed() {
    return INSTANCE;
  }

  @Override
  public boolean test(HttpServletRequest request, HttpServletResponse response) {
    return HttpStatus.valueOf(response.getStatus()).isError();
  }
}
