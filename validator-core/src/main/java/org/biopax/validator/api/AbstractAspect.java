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

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * Basic class for the framework classes 
 * that use AOP (and LTW) to check BioPAX
 * and report errors.
 *
 * @author rodche
 */
@Configurable
public abstract class AbstractAspect {
	private static final Log log = LogFactory.getLog(AbstractAspect.class);
	
	@Autowired
	protected Validator validator;

	@Autowired
	protected ValidatorUtils utils;
	
	
    /**
     * Registers the error in the validator.
     * 
     * @param object a model element or parser/reader (e.g., InputStream) associated with the issue
     * @param errorCode
     * @param reportedBy validation rule class name
     * @param setFixed
     * @param msgArgs
     */
    public void report(Object object, String errorCode, 
    		String reportedBy, boolean setFixed, Object... msgArgs) 
    {	
    	validator.report(object, errorCode, reportedBy, setFixed, msgArgs);
	}
    
    
	/**
	 * Registers other (external) exceptions.
	 * 
	 * The exception class, i.e., simple name in lower case, 
	 * is used as the error code, and the 'object' is to
	 * find the corresponding validation result where this 
	 * problem should be added.
	 * 
	 * This must be public method (for unclear reason, otherwise causes an AOP exception...)
	 * 
	 * @param t
	 * @param obj model, element, or another related to the BioPAX data object
	 * @param errorCode
	 * @param reportedBy 
	 * @param details extra message to be added at the end of the original error message if not null
	 */
    public void reportException(Throwable t, Object obj, String errorCode, String reportedBy, String details) {
    	
		StringBuilder msg = new StringBuilder(t.toString());
		
		if(t instanceof XMLStreamException) {
			XMLStreamException ex = (XMLStreamException) t;
			msg.append("; ").append(ex.getLocation().toString());
		} 
		else {
			if("exception".equals(errorCode)) //catch a bug
				msg.append(" - stack:").append(getStackTrace(t)).append(" - ");
		}
		
		if(details != null) 
			msg.append("; ").append(details);
		
		if (validator != null) {
			validator.report(obj, errorCode, reportedBy, false, msg.toString());
		} else {
			log.error("utils is null (not initialized?); skipping " +
					"an intercepted 'syntax.error': " + msg.toString() 
					+ " reported by: " + reportedBy);
		}
	}
    
    private static String getStackTrace(Throwable aThrowable) {
    	StringWriter strw = new StringWriter();
        PrintWriter printWriter = new PrintWriter(strw);
        aThrowable.printStackTrace(printWriter);
        return strw.toString();
    }
}
