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
