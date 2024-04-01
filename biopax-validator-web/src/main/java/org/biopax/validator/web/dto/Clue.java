package org.biopax.validator.web.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Suggester data transfer object (DTO). which describes
 * recommended values for a BioPAX property in given context.
 */
public class Clue {
  //additional comments (anything)
  private String info;
  //property type, value, etc. suggestions
  private List<Object> values;

  public Clue(){
    this.values = new ArrayList<>();
  }

  public Clue(String info) {
    this();
    this.info = info;
  }

  public String getInfo() {
    return info;
  }

  public void setInfo(String info) {
    this.info = info;
  }

  public List<Object> getValues() {
    return values;
  }

  public void setValues(List<Object> values) {
    this.values = values;
  }

  public boolean addValue(Object v) {
    return values.add(v);
  }
}
