package org.biopax.validator.api.beans;

import jakarta.xml.bind.annotation.*;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;

@XmlType(name="ErrorCaseType")
@XmlAccessorType(XmlAccessType.FIELD)
public class ErrorCaseType implements Serializable, Comparable<ErrorCaseType> {

  private static final long serialVersionUID = 1L;

  @XmlElement
  protected String message;
  @XmlAttribute
  protected String object;
  @XmlAttribute
  protected String reportedBy;
  @XmlAttribute
  protected boolean fixed;

  public ErrorCaseType() {
  }

  public ErrorCaseType(String reportedBy, String object, String msg) {
    this.reportedBy = reportedBy;
    this.object = object; //that we validated and where found the problem
    this.message = msg;
    this.fixed = false;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getObject() {
    return object;
  }

  public void setObject(String object) {
    this.object = object;
  }

  public String getReportedBy() {
    return reportedBy;
  }

  public void setReportedBy(String reportedBy) {
    this.reportedBy = reportedBy;
  }

  public boolean isFixed() {
    return fixed;
  }

  public void setFixed(boolean fixed) {
    this.fixed = fixed;
  }

  public String toString() {
    return String.format("(obj: %s, by: %s, msg: %s)",
      getObject(), getReportedBy(), getMessage());
  }

  public int compareTo(ErrorCaseType o) {
    //we only care to list error cases for the same object together
    return String.valueOf(getObject()).compareToIgnoreCase(o.getObject());
  }

  @Override
  public boolean equals(Object obj) {
    return (obj instanceof ErrorCaseType)
      ? String.valueOf(getObject()).equalsIgnoreCase(((ErrorCaseType)obj).getObject())
      && String.valueOf(getReportedBy()).equalsIgnoreCase(((ErrorCaseType)obj).getReportedBy())
      : false;
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(getObject()).append(getReportedBy()).build();
  }
}
