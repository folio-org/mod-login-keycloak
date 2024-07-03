package org.folio.login.controller.cookie.filter.config;

import static org.folio.login.controller.cookie.filter.predicate.UrlEqualsPredicate.urlEquals;

import java.util.function.BiPredicate;
import org.folio.login.controller.cookie.filter.HttpRequestResponseHolder;
import org.folio.login.controller.cookie.filter.InvalidateCookiesFilter;
import org.folio.login.controller.cookie.filter.predicate.RequestHttpMethodPredicate;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

@Configuration
public class FilterConfig {

  private static final String URL_TOKEN = "/authn/token";
  private static final String URL_LOGOUT = "/authn/logout";
  private static final String URL_LOGOUT_ALL = "/authn/logout-all";

  @Bean
  public FilterRegistrationBean<InvalidateCookiesFilter> invalidateCookiesFilter() {
    FilterRegistrationBean<InvalidateCookiesFilter> registrationBean = new FilterRegistrationBean<>();

    registrationBean.setFilter(new InvalidateCookiesFilter(
      post(URL_LOGOUT)
        .or(post(URL_LOGOUT_ALL))
        .or(get(URL_TOKEN))
    ));
    registrationBean.addUrlPatterns(
      URL_TOKEN,
      URL_LOGOUT,
      URL_LOGOUT_ALL);
    registrationBean.setOrder(InvalidateCookiesFilter.ORDER);

    return registrationBean;
  }

  private static BiPredicate<HttpRequestResponseHolder, Exception> post(String url) {
    return new RequestHttpMethodPredicate(HttpMethod.POST).and(urlEquals(url));
  }

  private static BiPredicate<HttpRequestResponseHolder, Exception> get(String url) {
    return new RequestHttpMethodPredicate(HttpMethod.GET).and(urlEquals(url));
  }
}
