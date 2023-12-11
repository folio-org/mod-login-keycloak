package org.folio.login.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor(staticName = "of")
public class PasswordCredential {

  private boolean temporary;
  private String type;
  private String value;
}
