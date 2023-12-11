package org.folio.login.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor(staticName = "of")
public class UserCredentials {

  private String id;
  private String type;
}
