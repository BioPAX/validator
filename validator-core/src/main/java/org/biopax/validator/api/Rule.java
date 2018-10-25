package org.biopax.validator.api;

import org.biopax.validator.api.beans.Validation;


/**
 * A validation rule.
 *
 * @param <T> a type of a model object or a data parser, stream, etc.
 *
 * @author rodche
 */
public interface Rule<T> {

  /**
   * Validates the object.
   *
   * @param validation the object where the model, validation settings and errors are stored
   * @param thing      to validate
   */
  void check(Validation validation, T thing);

  /**
   * Can check it?
   *
   * @param thing an object to validate
   * @return True when this rule can check the object.
   */
  boolean canCheck(Object thing);


  /**
   * Saves the error or warning that occurred or was fixed.
   * Call this method from a validation rule implementation
   * every time after a problem is found and/or fixed.
   *
   * @param validation the object where the model, validation settings and errors are stored
   * @param object     that is invalid or caused the error
   * @param code       error code, e.g., 'illegal.value'
   * @param setFixed   true/false - whether the issue was auto-fixed or not
   * @param args       extra parameters for the error message template
   */
  void error(Validation validation, Object object, String code, boolean setFixed, Object... args);

}
