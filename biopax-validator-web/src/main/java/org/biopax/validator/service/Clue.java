package org.biopax.validator.service;

import java.util.Map;

/**
 * Suggester data transfer object. which describes
 * recommended values for a BioPAX property in given context.
 */
public final class Clue {
  //property access/context path (expression)
  String path;

  //additional comments (anything)
  String info;

  //property type, value, etc. suggestions
  Map<String,Object>[] values; //TODO: switch from using Map[] to some DTOs once query/result schema is decided...
}
