package org.folio.login.integration.users.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.Data;
import org.folio.common.domain.model.AnyDescriptor;
import org.springframework.format.annotation.DateTimeFormat;

@Data
public class User {

  private String username;
  private UUID id;
  private String externalSystemId;
  private String barcode;
  private Boolean active;
  private String type;
  private UUID patronGroup;

  private Set<UUID> departments = new LinkedHashSet<>();
  private List<String> proxyFor = new ArrayList<>();
  private Personal personal;
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private Date enrollmentDate;
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private Date expirationDate;
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private Date createdDate;
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private Date updatedDate;
  private AnyDescriptor metadata;
  private Map<String, Object> customFields = new HashMap<>();

  public User username(String username) {
    this.username = username;
    return this;
  }

  public User id(UUID id) {
    this.id = id;
    return this;
  }

  public User externalSystemId(String externalSystemId) {
    this.externalSystemId = externalSystemId;
    return this;
  }

  public User barcode(String barcode) {
    this.barcode = barcode;
    return this;
  }

  public User active(Boolean active) {
    this.active = active;
    return this;
  }

  public User type(String type) {
    this.type = type;
    return this;
  }

  public User patronGroup(UUID patronGroup) {
    this.patronGroup = patronGroup;
    return this;
  }

  public User departments(Set<UUID> departments) {
    this.departments = departments;
    return this;
  }

  public User addDepartmentsItem(UUID departmentsItem) {
    if (this.departments == null) {
      this.departments = new LinkedHashSet<>();
    }
    this.departments.add(departmentsItem);
    return this;
  }

  public User proxyFor(List<String> proxyFor) {
    this.proxyFor = proxyFor;
    return this;
  }

  public User addProxyForItem(String proxyForItem) {
    if (this.proxyFor == null) {
      this.proxyFor = new ArrayList<>();
    }
    this.proxyFor.add(proxyForItem);
    return this;
  }

  public User personal(Personal personal) {
    this.personal = personal;
    return this;
  }

  public User enrollmentDate(Date enrollmentDate) {
    this.enrollmentDate = enrollmentDate;
    return this;
  }

  public User expirationDate(Date expirationDate) {
    this.expirationDate = expirationDate;
    return this;
  }

  public User createdDate(Date createdDate) {
    this.createdDate = createdDate;
    return this;
  }

  public User updatedDate(Date updatedDate) {
    this.updatedDate = updatedDate;
    return this;
  }

  public User metadata(AnyDescriptor metadata) {
    this.metadata = metadata;
    return this;
  }

  public User customFields(Map<String, Object> customFields) {
    this.customFields = customFields;
    return this;
  }

  public User putCustomFieldsItem(String key, Object customFieldsItem) {
    if (this.customFields == null) {
      this.customFields = new HashMap<>();
    }
    this.customFields.put(key, customFieldsItem);
    return this;
  }
}
