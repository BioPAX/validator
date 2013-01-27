package org.biopax.validator.api;

/*
 * #%L
 * Object Model Validator Core
 * %%
 * Copyright (C) 2008 - 2013 University of Toronto (baderlab.org) and Memorial Sloan-Kettering Cancer Center (cbio.mskcc.org)
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import java.io.InputStream;
import java.util.Collection;
import java.util.Set;

import org.biopax.validator.api.beans.Validation;

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
	 * Given the object, finds
	 * the corresponding validations
	 * (that it's been associated with).
	 * 
	 * @param obj a BioPAX element, Model, or even SimpleIOHandler
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
     * to registered validation objects associated with the object.
	 * 
	 * @param obj object associated with a validation result (can be even InputStream, during import, but usually is a BioPAX element)
	 * @param errorCode error code
	 * @param reportedBy class name of a validation rule or a name of another BioPAX validating class, method (e.g., AOP aspect's method/joinpoint name).
	 * @param isFixed if true, - find and set the attribute
	 * @param args
	 */
	void report(Object obj, String errorCode, String reportedBy, boolean isFixed, Object... args);
	
}
