package org.biopax.validator.web.dto;

import javax.validation.constraints.NotBlank;

public class Xref {
  @NotBlank
  private String db;
  @NotBlank
  private String id;

  private String uri;
  private boolean dbOk;
  private boolean idOk;
  private String  preferredDb;
  private String  namespace;

  public String getDb() {
    return db;
  }

  public void setDb(String db) {
    this.db = db;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getUri() {
    return uri;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }

  public boolean isDbOk() {
    return dbOk;
  }

  public void setDbOk(boolean dbOk) {
    this.dbOk = dbOk;
  }

  public boolean isIdOk() {
    return idOk;
  }

  public void setIdOk(boolean idOk) {
    this.idOk = idOk;
  }

  public String getPreferredDb() {
    return preferredDb;
  }

  public void setPreferredDb(String preferredDb) {
    this.preferredDb = preferredDb;
  }

  public String getNamespace() {
    return namespace;
  }

  public void setNamespace(String namespace) {
    this.namespace = namespace;
  }
}
