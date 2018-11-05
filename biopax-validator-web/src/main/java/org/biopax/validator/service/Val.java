package org.biopax.validator.service;

import java.util.Map;

/**
 * Data transfer object, which describes a
 * suggested value in a BioPAX property context.
 */
public final class Val<T> {

  private final Class<T> clazz;

  String type;

  Map<String,Object> data;

  public Val(Class<T> clazz) {
    this.clazz = clazz;
    this.type = clazz.getSimpleName();
  }
}
