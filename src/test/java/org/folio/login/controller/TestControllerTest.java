package org.folio.login.controller;

import static java.time.Instant.ofEpochSecond;
import static org.springframework.http.HttpHeaders.SET_COOKIE;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
import java.lang.annotation.Annotation;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ArrayUtils;
import org.folio.login.controller.TestControllerTest.TestResponseBodyAdvice;
import org.folio.test.types.UnitTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultHandler;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@UnitTest
@Log4j2
@Import({ApiExceptionHandler.class, TestResponseBodyAdvice.class})
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
      .andExpectAll(cookie().value(c1.getName(), c1.getValue()), cookie().maxAge(c1.getName(), 0))
      .andExpectAll(cookie().value(c2.getName(), c2.getValue()), cookie().maxAge(c2.getName(), 0));
  }

  protected static ResultHandler logResponseBody() {
    return result -> {
      var response = result.getResponse();
      log.info("[Headers] \n\t{}", response.getHeaderNames().stream()
        .flatMap(name -> response.getHeaders(name).stream().map(value -> name + ": " + value))
        .collect(Collectors.joining("\n\t"))
      );
      log.info("[Res-Body] {}", response.getContentAsString());
    };
  }

  @ControllerAdvice
  public static final class TestResponseBodyAdvice implements ResponseBodyAdvice<ResponseEntity<?>> {

    private static final long EXPIRED_DATE_IN_SECONDS = ofEpochSecond(0).toEpochMilli() * 1000;

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
      for (Annotation a : returnType.getMethodAnnotations()) {
        if (a.annotationType() == InvalidateCookies.class) {
          return true;
        }
      }

      return false;
    }

    @Override
    public ResponseEntity<?> beforeBodyWrite(ResponseEntity<?> body, MethodParameter returnType,
      MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType,
      ServerHttpRequest request, ServerHttpResponse response) {

      var cookies = ((ServletServerHttpRequest) request).getServletRequest().getCookies();

      if (ArrayUtils.isNotEmpty(cookies)) {
        for (int i = 0; i < cookies.length; i++) {
          response.getHeaders()
            .add(SET_COOKIE, createHeader(cookies[i].getName(), cookies[i].getValue()));
        }
      }

      return body;
    }

    private String createHeader(String name, String value) {
      return ResponseCookie.from(name, value)
        .maxAge(EXPIRED_DATE_IN_SECONDS)
        .build()
        .toString();
    }
  }
}
