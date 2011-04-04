package org.biopax.validator.impl;


import javax.xml.stream.XMLStreamException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.validator.result.Behavior;
import org.biopax.validator.Validator;
import org.biopax.validator.result.ErrorType;
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
     * @param obj associated with a validation result objects (can be even InputStream, during import, but usually is a BioPAX element)
     * @param objectName
     * @param errorCode
     * @param ruleName
     * @param warnOrErr
     * @param setFixed
     * @param msgArgs
     * @param error
     */
    public void report(Object obj, String objectName, 
    		String errorCode, String ruleName, Behavior warnOrErr, 
    		boolean setFixed, Object... msgArgs) 
    {	
    	ErrorType error = utils.createError(objectName, errorCode, ruleName, warnOrErr, msgArgs);
    	validator.report(obj, error, setFixed);
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
	 * @param args optional message arguments (to be added as text at the end of the original error message)
	 */
    public void reportException(Throwable t, Object obj, Object... args) {
    	final String rule = "interceptor";
		String msg = 
			(t.getMessage()==null || "".equalsIgnoreCase(t.getMessage()))
				? t.getClass().getSimpleName() : t.getMessage();

		String id;
		if(obj instanceof SimpleIOHandler)
		{
			SimpleIOHandler r = (SimpleIOHandler) obj;
			id = ""; 
	    	try {
	    		id = r.getId();
	    	} catch (Throwable e) {
	    		id = "reader";
			}
		} else 
		{
			id = BiopaxValidatorUtils.getId(obj);
		}
		
		if(t instanceof XMLStreamException) {
			XMLStreamException ex = (XMLStreamException) t;
			msg += "; "  + ex.getLocation().toString();
		} else if(t instanceof BiopaxValidatorException) {
			msg += "; " + 
				((BiopaxValidatorException)t).getMsgArgs().toString();
		}
		
		if (utils != null) {
			if(args.length>0) msg += "; " + BiopaxValidatorUtils.toString(args);
			ErrorType error = utils.createError(id, "syntax.error", rule, Behavior.ERROR, msg);
			validator.report(obj, error, false);
		}
		
		if(log.isTraceEnabled()) {
			log.trace("reportException (validator bean= "
					+ validator	+"): " + msg, t);
		}

	}
}
