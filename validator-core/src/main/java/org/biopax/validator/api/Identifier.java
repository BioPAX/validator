package org.biopax.validator.api;

/**
 * This is a generic functional object (strategy)
 * interface that contain the only method, which
 * is called by the validator framework to get an
 * identifier or name of a model element to report
 * an error about.
 *
 * Not BioPAX specific.
 *
 * @author rodche
 *
 */
public interface Identifier {

  /**
   * Gets the id or name of a model element or
   * other object that matters during a validation
   * and worth mentioning in a error message.
   *
   * This is called by the Validator framework to get a
   * domain specific identifier or name of a model element
   * to report an error about.
   *
   * This interface is required, because a domain specific 'get id'
   * method, if any, is not known to the core validation framework
   * in advance, and toString method may not be satisfactory.
   *
   * @param obj a model element or, e.g., stream
   * @return id
   */
  String identify(Object obj);

}
