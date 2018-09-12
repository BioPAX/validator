package org.biopax.validator.api;

/*
 *
 */

import org.biopax.validator.api.beans.Validation;


/**
 * Validation rule interface.
 * 
 * Although at run time T is a BioPAXElement
 * (this is 100% true for the current version),
 * at compile time, when implementing a rule, one may
 * prefer to use other interfaces as well, e.g.,
 * Process or Named. Therefore, "T extends BioPAXElement"
 * is not used here.
 *
 * @author rodche
 */
public interface Rule<T> {  

	/**
     * Validates the object.
	 * @param validation the object where the model, validation settings and errors are stored
	 * @param thing to validate
	 * @param fix try to fix the error case if found
     */
	void check(Validation validation, T thing);

    /**
     * Can check it?
     *
     * @param thing
     * @return True when it can.
     */
    boolean canCheck(Object thing);
    
    
    /**
     * Saves the error or warning that occurred or was fixed.
     * 
     * Call this method from a validation rule implementation 
     * every time after a problem is found and/or fixed. 
	 * 
	 * @param validation the object where the model, validation settings and errors are stored
	 * @param object that is invalid or caused the error
	 * @param code error code, e.g., 'illegal.value'
	 * @param setFixed 
	 * @param args extra parameters for the error message template
	 */
    void error(Validation validation, Object object, String code, boolean setFixed, Object... args);
    
}
