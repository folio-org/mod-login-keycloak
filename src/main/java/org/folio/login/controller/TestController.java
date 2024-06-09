package org.folio.login.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
public class TestController {

  @PostMapping("/test")
  @InvalidateCookies
  public ResponseEntity<Void> create(HttpServletRequest request) {
    var cookies = request.getCookies();
    if (cookies != null) {
      for (var cookie : cookies) {
        log.info("{} = {}, maxAge = {}", cookie.getName(), cookie.getValue(), cookie.getMaxAge());
      }
    }

    return ResponseEntity.noContent().build();
  }

  @GetMapping("/test")
  public ResponseEntity<Test> get(HttpServletRequest request) {
    var cookies = request.getCookies();
    if (cookies != null) {
      for (var cookie : cookies) {
        log.info("{} = {}, maxAge = {}", cookie.getName(), cookie.getValue(), cookie.getMaxAge());
      }
    }

    return ResponseEntity.ok(new Test(UUID.randomUUID(), "body"));
  }

  public record Test(UUID id, String name) {}
}
