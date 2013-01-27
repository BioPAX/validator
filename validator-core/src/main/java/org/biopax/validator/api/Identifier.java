/**
 * 
 */
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
	 * @param obj
	 * @return
	 */
	String identify(Object obj);

}
