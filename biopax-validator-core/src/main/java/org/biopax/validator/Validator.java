package org.biopax.validator;

import java.io.InputStream;
import java.util.Collection;
import java.util.Set;

import org.biopax.validator.result.ErrorType;
import org.biopax.validator.result.Validation;

/**
 * BioPAX Validator interface
 * 
 * 
 * @author rodch
 *
 */
public interface Validator {
	
	/**
	 * Get all the currently loaded BioPAX rules (beans).
	 * 
	 * @return
	 */
	Set<Rule<?>> getRules();
	
	/**
	 * Get the BioPAX rule by its bean name.
	 * 
	 * @param ruleName
	 * @return
	 */
	Rule<?> findRuleByName(String ruleName);
	
	
	/**
	 * Gets all the currently registered validation results.
	 * 
	 * @return
	 */
	Collection<Validation> getResults();
	
	
	/**
	 * Associates a validation report with the BioPAX data
	 * and creates the in-memory model (Paxtools Model).
	 * 
	 * The validation result is object where all the problems 
	 * related to this data instance are collected.
	 * 
	 * @param validation
	 * @param inputStream
	 */
	void importModel(Validation validation, InputStream inputStream);

	/**
	 * Associates an object, such as BioPAX model, element, 
	 * InputStream; with the given validation result.
	 * 
	 * Some errors may occur during the data import
	 * but before the model is created, e.g., 
	 * those in the XML/RDF header or software bugs.
	 * And we want to report these problems as well.
	 * 
	 * @param element object (e.g., existing Model)
	 * @param validation 
	 */
	void associate(Object element, Validation validation);
	
	
	/**
	 * Associates, e.g., a property value with validation results
	 * by using the 'parent' element to find it. This allows for
	 * errors to be reported while model is being read,
	 * maybe before the child element is added to the model.
	 * 
	 * @param parent BioPAX element
	 * @param child value
	 */
	void indirectlyAssociate(Object parent, Object child);
	
	
	/**
	 * Removes the object from all the validations, 
	 * so that it won't be (directly) associated with any  
	 * result anymore. However, the element can still be a member 
	 * of several models that are registered with the validator, 
	 * and there is no problem to successfully report errors. 
	 * 
	 * @param o element to release
	 */
	void freeObject(Object o);
	
	/**
	 * Given the object, finds
	 * the corresponding validations
	 * (that it's been associated with).
	 * 
	 * @param obj a BioPAX element, Model, or even SimpleReader
	 * @return
	 */
	Collection<Validation> findValidation(Object obj);
	
	/**
	 * Post-model validation. 
	 * 
	 * The result also includes other  
	 * problems that might occur during 
	 * the model initialization and creation.
	 * 
	 * @param validation
	 */
	void validate(Validation validation);


	
    /**
     * Adds the validation error (with proper attributes)
     * to corresponding registered validation objects.
     * 
     * @param obj associated with a validation result objects (can be even InputStream, during import, but usually is a BioPAX element)
     * @param error
     * @param setFixed if true, - find and set the attribute
     */
	void report(Object obj, ErrorType error, boolean setFixed);
	
}
