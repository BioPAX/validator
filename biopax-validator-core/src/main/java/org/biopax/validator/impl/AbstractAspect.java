package org.biopax.validator.impl;

import java.util.Collection;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import org.biopax.paxtools.io.simpleIO.SimpleReader;
import org.biopax.validator.Behavior;
import org.biopax.validator.result.ErrorType;
import org.biopax.validator.result.Validation;
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
	protected BiopaxValidatorUtils utils;
			
	@Autowired
	protected ValidatorImpl validator;

	
    /**
     * Registers the error with the proper attributes.
     * 
     * @param models
     * @param error
     */
    public void report(Collection<Validation> keys, ErrorType error) {
		// Skip 'ignored' ones
		if (utils.isIgnoredCode(error.getCode())) {
			return;
		}
		// add to the corresponding validation result
		for(Validation result: keys) { 
			if(log.isTraceEnabled()) {
				log.trace("reports: " + error.toString() 
						+ " "+ error.getErrorCase().toArray()[0] + " in: " + result.getDescription());
			}
			result.addError(error);
		}
	}
        
	/**
	 * Registers external exceptions.
	 * 
	 * The exception class, i.e., simple name in lower case, 
	 * is used as the error code, and the 'object' is to
	 * find the corresponding validation result where this 
	 * problem should be added.
	 * 
	 * @param t
	 * @param obj model, element, or another related to the BioPAX data object
	 * @param args optional message arguments (to be added as text
	 *  at the end of the original error message)
	 */
    public void reportException(Throwable t, Object obj, Object... args) {
    	final String rule = "interceptor";
		String msg = 
			(t.getMessage()==null || "".equalsIgnoreCase(t.getMessage()))
				? t.getClass().getSimpleName() : t.getMessage();

		String id;
		if(obj instanceof SimpleReader)
		{
			SimpleReader r = (SimpleReader) obj;
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
		}
		
		if (utils != null) {
			if(args.length>0) msg += "; " + BiopaxValidatorUtils.toString(args);
			ErrorType error = utils.createError(
				id, "syntax.error", rule, Behavior.ERROR, msg);
			Collection<Validation> keys = validator.findKey(obj);
			if(!keys.isEmpty()) {
				report(keys, error);
			} else { 
				// the object is not associated neither with parser nor model
				log.warn("For object " +
						obj + ", user won't see this message: " + msg);
			}
		}
		
		if(log.isTraceEnabled()) {
			log.trace("reportException: " + msg, t);
		}

	}
}
