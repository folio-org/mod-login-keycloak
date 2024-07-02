package org.folio.login.integration.kafka.event;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LogoutEvent {

  private String userId;
  private String keycloakUserId;
  private String sessionId;
  private Type type;

  public enum Type {
    LOGOUT,
    LOGOUT_ALL
  }
}
