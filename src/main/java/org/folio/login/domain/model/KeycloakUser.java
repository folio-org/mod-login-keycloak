package org.folio.login.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class KeycloakUser {

  public static final String USER_ID_ATTR = "user_id";

  private String id;
  @JsonProperty("username")
  private String userName;
  private String firstName;
  private String lastName;
  private String email;
  private Boolean emailVerified;
  private Long createdTimestamp;
  private Boolean enabled;
  private Map<String, List<String>> attributes = new HashMap<>();
}
