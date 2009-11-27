package org.biopax.validator;

import java.io.InputStream;
import java.util.Collection;
import java.util.Set;

import org.biopax.paxtools.model.Model;
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
	 * Assigns the name for the BioPAX data, 
	 * associates a validation report with it,
	 * and creates the in-memory model.
	 * 
	 * The name is a key to the validation result 
	 * where all the problems related to this data
	 * are reported.
	 * 
	 * @param validation
	 * @param inputStream
	 */
	void importModel(Validation validation, InputStream inputStream);

	/**
	 * Assigns the name for the existing BioPAX Model 
	 * and associates a new validation report with it.
	 * 
	 * The name is a key to the validation result 
	 * where all the problems related to this data
	 * are reported.
	 * 
	 * @param validation
	 * @param model
	 */
	void addModel(Validation validation, Model model);

	/**
	 * Associates an object, such as BioPAX model or InputStream,
	 * with the given name and corresponding validation report.
	 * 
	 * There are errors that may occur during the data import
	 * but before the actual model is created, e.g., 
	 * those in the XML/RDF header or software bugs.
	 * However, we want to report these kind of problems as well.
	 * 
	 * @param element object (e.g., existing Model)
	 * @param validation 
	 */
	void associate(Object element, Validation validation);
	
	
	/**
	 * Associate a property value with a validation result(s)
	 * using the parent element to find the key. This allows for
	 * errors to be reported while model is being read,
	 * maybe even before the child element is added to the model.
	 * 
	 * @param parent BioPAX element
	 * @param child value
	 */
	void indirectlyAssociate(Object parent, Object child);
	
	
	/**
	 * Checks whether the validator contains the key (unique model name),
	 * and thus the associated objects in its registry.
	 * 
	 * @param key Validation
	 * @return
	 */
	boolean contains(Validation key);
	

	/**
	 * Removes the object from the corresponding registry,
	 * so it won't be forcedly associated with the model 
	 * and its validation result anymore.
	 * 
	 * @param o element to "forget"
	 * @param key validation object
	 */
	void free(Object o, Validation key);
	
	
	/**
	 * Removes the object from the internal 'objects' registry, 
	 * so that it won't be artificially (forcedly) associated with validation 
	 * results anymore. However, the element can still be a member 
	 * of several models that are registered with the validator, there
	 * won't be a problem to find where to report errors. 
	 * 
	 * @param o element to de-associate
	 */
	void freeObject(Object o);
	
	/**
	 * Given the object, finds keys for 
	 * the corresponding validation results.
	 * 
	 * @param obj a BioPAX element, Model, or even SimpleReader
	 * @return
	 */
	Collection<Validation> findKey(Object obj);
	
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
	 * Forget the validation result
	 * (this won't necessarily destroy it).
	 * 
	 * @param key
	 */
	void free(Validation key);
	
	/**
	 * Get associated model(s)
	 * 
	 * @param key
	 * @return
	 */
	public Collection<Model> getModel(Validation key);
	
}
