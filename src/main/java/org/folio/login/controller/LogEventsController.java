package org.folio.login.controller;

import lombok.RequiredArgsConstructor;
import org.folio.login.domain.dto.LogEventCollection;
import org.folio.login.rest.resource.LogEventsApi;
import org.folio.login.service.LogEventsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class LogEventsController implements LogEventsApi {
  private final LogEventsService service;

  @Override
  public ResponseEntity<LogEventCollection> getLogEvents(Integer length, Integer start, String query) {
    var events = service.getUserEvents(start - 1, length, query);
    return ResponseEntity.ok(events);
  }
}
