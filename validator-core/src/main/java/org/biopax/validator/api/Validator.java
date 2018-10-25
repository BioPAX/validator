package org.biopax.validator.api;


import java.io.InputStream;
import java.util.Collection;
import java.util.Set;

import org.biopax.validator.api.beans.Validation;

/**
 * Object model validator.
 *
 * @author rodche
 */
public interface Validator {

  /**
   * Gets all the active validation rules.
   *
   * @return available (loaded) validation rules
   */
  Set<Rule<?>> getRules();


  /**
   * Gets all the registered validation results (where error cases get stored).
   *
   * @return results
   */
  Collection<Validation> getResults();


  /**
   * Associates a validation report with the data and creates in-memory model.
   *
   * The validation result is object where all the problems
   * related to this data instance are collected.
   *
   * @param validation validation result object
   * @param inputStream data input stream
   */
  void importModel(Validation validation, InputStream inputStream);

  /**
   * Associates a model, element, or stream with the validation result.
   *
   * Some errors may occur during the data import and parsing before the model is created,
   * e.g., in XML headers, software bugs. We want to report these problems as well.
   *
   * @param element object (e.g., existing Model)
   * @param validation results object
   */
  void associate(Object element, Validation validation);


  /**
   * Associates, e.g., a property value with validation results
   * by using the 'parent' element to find it. This allows for
   * errors to be reported while model is being read,
   * maybe before the child element is added to the model.
   *
   * @param parent model element
   * @param child value
   */
  void indirectlyAssociate(Object parent, Object child);


  /**
   * Given the object, finds
   * the corresponding validations
   * (that it's been associated with).
   *
   * @param obj a model, element or even I/O handler
   * @return validation results
   */
  Collection<Validation> findValidation(Object obj);


  /**
   * Post-model validation.
   *
   * The result also includes other
   * problems that might occur during
   * the model initialization and creation.
   *
   * @param validation results object
   */
  void validate(Validation validation);


  /**
   * Adds the validation error (with proper attributes)
   * to registered validation objects associated with the object.
   *
   * @param obj object associated with a validation result (can be even stream, during import, but usually is a model element)
   * @param errorCode error code
   * @param reportedBy class name of a validation rule or a name of another validating class or method (AOP aspect method, jointpoint).
   * @param isFixed if true, - find and set the attribute
   * @param args additional message parameters (details)
   */
  void report(Object obj, String errorCode, String reportedBy, boolean isFixed, Object... args);

}
