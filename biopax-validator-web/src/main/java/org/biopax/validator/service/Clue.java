package org.biopax.validator.service;

/**
 * Suggester data transfer object. which describes
 * recommended values for a BioPAX property in given context.
 */
public final class Clue {
  //property access/context path (expression)
  String path;
  //property value suggestions
  Val<?>[] values;
  //additional comments (anything)
  String info;
}
