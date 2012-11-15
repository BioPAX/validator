package org.biopax.validator.impl;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.biopax.validator.Validator;
import org.biopax.validator.utils.BiopaxValidatorException;
import org.biopax.validator.utils.BiopaxValidatorUtils;

/**
 * Basic class for the framework classes 
 * that use AOP (and LTW) to check BioPAX
 * and report errors.
 *
 * @author rodche
 */
@Configurable
abstract class AbstractAspect {
	private static final Log log = LogFactory.getLog(AbstractAspect.class);
	
	@Autowired
	protected Validator validator;

	@Autowired
	protected BiopaxValidatorUtils utils;
	
	
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
	 * @param args optional message arguments (to be added as text at the end of the original error message)
	 */
    public void reportException(Throwable t, Object obj, String errorCode, String reportedBy, Object... args) {
    	
		StringBuilder msg = new StringBuilder(
				(t.getMessage()==null || "".equalsIgnoreCase(t.getMessage()))
					? t.getClass().getSimpleName() 
						: t.getMessage()
				);
		
		if(t instanceof XMLStreamException) {
			XMLStreamException ex = (XMLStreamException) t;
			msg.append("; ").append(ex.getLocation().toString());
		} else if(t instanceof BiopaxValidatorException) {
			msg.append("; ").append(((BiopaxValidatorException)t).getMsgArgs().toString());
		} else {
   			msg.append(" - stack:").append(getStackTrace(t)).append(" - ");
		}
		
		if(args.length>0) 
			msg.append("; ").append(BiopaxValidatorUtils.errorMsgArgument(args));
		
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
