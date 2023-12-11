package org.folio.login.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Date;
import java.util.UUID;
import lombok.Data;

@Data
@Entity
@Table(name = "auth_password_action")
public class PasswordCreateActionEntity {

  @Id
  private UUID id;

  @Column(name = "user_id", unique = true)
  private UUID userId;

  @Column(name = "expiration_time")
  private Date expirationTime;
}
