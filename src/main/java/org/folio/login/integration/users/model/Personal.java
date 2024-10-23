package org.folio.login.integration.users.model;

import java.net.URI;
import java.util.Date;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

@Data
public class Personal {

  private String lastName;
  private String firstName;
  private String middleName;
  private String preferredFirstName;
  private String email;
  private String phone;
  private String mobilePhone;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private Date dateOfBirth;

  private String preferredContactTypeId;

  private URI profilePictureLink;

  public Personal lastName(String lastName) {
    this.lastName = lastName;
    return this;
  }

  public Personal firstName(String firstName) {
    this.firstName = firstName;
    return this;
  }

  public Personal middleName(String middleName) {
    this.middleName = middleName;
    return this;
  }

  public Personal preferredFirstName(String preferredFirstName) {
    this.preferredFirstName = preferredFirstName;
    return this;
  }

  public Personal email(String email) {
    this.email = email;
    return this;
  }

  public Personal phone(String phone) {
    this.phone = phone;
    return this;
  }

  public Personal mobilePhone(String mobilePhone) {
    this.mobilePhone = mobilePhone;
    return this;
  }

  public Personal dateOfBirth(Date dateOfBirth) {
    this.dateOfBirth = dateOfBirth;
    return this;
  }

  public Personal preferredContactTypeId(String preferredContactTypeId) {
    this.preferredContactTypeId = preferredContactTypeId;
    return this;
  }

  public Personal profilePictureLink(URI profilePictureLink) {
    this.profilePictureLink = profilePictureLink;
    return this;
  }
}
