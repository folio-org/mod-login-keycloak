package org.folio.login.controller.cookie;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.function.BiPredicate;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;

@RequiredArgsConstructor
public final class RequestHttpMethodPredicate implements BiPredicate<HttpServletRequest, HttpServletResponse> {

  private final HttpMethod httpMethod;

  @Override
  public boolean test(HttpServletRequest request, HttpServletResponse response) {
    return request.getMethod().equalsIgnoreCase(httpMethod.name());
  }
}
