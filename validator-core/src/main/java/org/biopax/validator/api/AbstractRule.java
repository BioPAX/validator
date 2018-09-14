package org.biopax.validator.api;

import java.util.Collection;

import org.biopax.validator.api.beans.ErrorType;
import org.biopax.validator.api.beans.Validation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Abstract validation rule.
 * 
 * @author rodche
 *
 * @param <T> a type the rule can validate, though it also depends on {@link #canCheck(Object)} method implementation
 */
public abstract class AbstractRule<T> implements Rule<T> {

    protected Log logger;

    @Autowired
    protected ValidatorUtils utils;
    
    
    public AbstractRule() {
        logger = LogFactory.getLog(this.getClass()); // concrete class
    }

    
    /**
     * {@inheritDoc}
     * 
     * Implementation of the interface method.
     * 
     * @throws NullPointerException when validation is null
	 */
    public void error(final Validation validation, Object object, String code, boolean setFixed, Object... args) {

    	if(object == null) {
   			logger.warn("The 'thing' (the error is about) is NULL! Skipping.");
    		return;
    	}
    	
    	// get object's ID
    	String thingId = validation.identify(object);
    	
    	// properly resolve extra message args to IDs
    	// w/o changing the no. args
    	args = parseArgs(validation, args);
    	   	
    	// create and add/update the error case using current validation profile
    	ErrorType error = (utils != null) 
    			? utils.createError(
    				thingId, code, getClass().getName(), validation.getProfile(), setFixed, args)
    			// when - no config. available (JUnit tests?); it will be 'ERROR' type with default messages:
    			: ValidatorUtils.createError(null, null,
    				thingId, code, getClass().getName(), null, setFixed, args);
    	
    	validation.addError(error);

   		logger.info( ((setFixed) ? "FIXED " : "") + " " + code + " in " + thingId);
    }
    
    
    /**
     * TODO
     * 
     * @param v
     * @param args
     * @return
     */
    private String[] parseArgs(Validation v, Object... args) {
    	String[] newArgs = new String[args.length];
    	
    	int i=0;
    	for(Object a: args) {
    		final StringBuilder sb = new StringBuilder();
    		
    		if(a instanceof Collection) {
    			for(Object o : (Collection)a) {
    				sb.append(v.identify(o)).append("; ");
    			}
    		} else if(a instanceof Object[]) {
    			for(Object o : (Object[])a) {
    				sb.append(v.identify(o)).append("; ");
    			}
    		} else {
    			sb.append(v.identify(a));
    		} 
    		
			String s = sb.toString();
			if (s.contains("{") || s.contains("}")) {
				s.replaceAll("\\}", ")");
				s.replaceAll("\\{", "(");
			}    		
    		newArgs[i] = s;
    		i++;
    	}
    	
    	return newArgs;
	}

}