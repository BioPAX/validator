package org.biopax.validator.api;

/*
 *
 */

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Basic class for the framework classes 
 * that use AOP (and LTW) to check BioPAX
 * and report errors.
 *
 * @author rodche
 */
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
     * @param errorCode error code
     * @param reportedBy validation rule class name
     * @param setFixed whether the problem was auto-fixed or not
     * @param msgArgs additional error message parameters
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
	 * @param t the exception
	 * @param obj model, element, or another related to the BioPAX data object
	 * @param errorCode validator error code
	 * @param reportedBy validator rule name
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
