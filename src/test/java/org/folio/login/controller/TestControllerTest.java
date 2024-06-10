package org.folio.login.controller;

import static org.springframework.http.HttpHeaders.SET_COOKIE;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;
import org.folio.login.controller.cookie.InvalidateCookiesResponseBodyAdvice;
import org.folio.test.types.UnitTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultHandler;
import org.springframework.test.web.servlet.ResultMatcher;

@UnitTest
@Log4j2
@Import({ApiExceptionHandler.class, InvalidateCookiesResponseBodyAdvice.class})
@WebMvcTest(TestController.class)
class TestControllerTest {

  @Autowired private MockMvc mockMvc;

  @Test
  void create_positive_withoutCookies() throws Exception {
    mockMvc.perform(post("/test")
        .contentType(APPLICATION_JSON))
      .andDo(logResponseBody())
      .andExpect(status().isNoContent())
      .andExpect(header().doesNotExist(SET_COOKIE));
  }

  @Test
  void create_positive_withCookie() throws Exception {
    var c1 = new Cookie("cookie1", "value1");
    c1.setMaxAge(100);

    var c2 = new Cookie("cookie2", "value2");
    c2.setMaxAge(200);

    mockMvc.perform(post("/test")
        .contentType(APPLICATION_JSON)
        .cookie(c1, c2))
      .andDo(logResponseBody())
      .andExpect(status().isNoContent())
      .andExpectAll(invalidatedCookie(c1))
      .andExpectAll(invalidatedCookie(c2));
  }

  @Test
  void create_negative_withCookie() throws Exception {
    var c1 = new Cookie("cookie1", "value1");
    c1.setMaxAge(100);

    var c2 = new Cookie("cookie2", "value2");
    c2.setMaxAge(200);

    mockMvc.perform(post("/test-failed")
        .contentType(APPLICATION_JSON)
        .cookie(c1, c2))
      .andDo(logResponseBody())
      .andExpect(status().isBadRequest())
      .andExpectAll(invalidatedCookie(c1))
      .andExpectAll(invalidatedCookie(c2));
  }

  private static ResultHandler logResponseBody() {
    return result -> {
      var response = result.getResponse();
      log.info("[Headers] \n\t{}", response.getHeaderNames().stream()
        .flatMap(name -> response.getHeaders(name).stream().map(value -> name + ": " + value))
        .collect(Collectors.joining("\n\t"))
      );
      log.info("[Res-Body] {}", response.getContentAsString());
    };
  }

  private static ResultMatcher[] invalidatedCookie(Cookie cookie) {
    ResultMatcher[] result = new ResultMatcher[2];
    result[0] = cookie().value(cookie.getName(), cookie.getValue());
    result[1] = cookie().maxAge(cookie.getName(), 0);

    return result;
  }
}
