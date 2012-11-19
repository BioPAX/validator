package org.biopax.validator.api;

import java.util.Arrays;

import org.biopax.validator.api.beans.ErrorType;
import org.biopax.validator.api.beans.Validation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * Abstract validation rule.
 * 
 * @author rodche
 *
 * @param <T> a type the rule can validate, though it also depends on {@link #canCheck(Object)} method implementation
 */
@Configurable
public abstract class AbstractRule<T> implements Rule<T> {

    protected Log logger = LogFactory.getLog(AbstractRule.class);

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
    @Override
    public void error(final Validation validation, Object object, String code, boolean setFixed, Object... args) {

    	if(object == null) {
   			logger.warn("The 'thing' (the error is about) is NULL! Skipping.");
    		return;
    	}
    	
    	// get object's ID
    	String thingId = validation.identify(object);
    	
    	// resolve/escape extra message args to IDs
    	args = fixMessageArgs(validation, args);
    	
    	
		if (validation.isMaxErrorsSet() 
				&& validation.getNotFixedErrors() > validation.getMaxErrors()) 
		{
			if(logger.isDebugEnabled()) {
				logger.debug("Max errors exceeded (" + validation.getMaxErrors() +
					", " + validation.getDescription() + "); Skipping for " + 
					code  + ", obj:" + thingId + "(fixed:" + setFixed + 
					"), args:" + Arrays.toString(args));
			}
			return;	
		}
  	    	
    	// create and add/update the error case using current validation profile
    	ErrorType error = (utils != null) 
    			? utils.createError(
    				thingId, code, getClass().getName(), validation.getProfile(), setFixed, args)
    			// when - no config. available (JUnit tests?); it will be 'ERROR' type with default messages:
    			: ValidatorUtils.createError(null, null,
    				thingId, code, getClass().getName(), null, setFixed, args);
    	
    	validation.addError(error);

    	if(logger.isDebugEnabled())
    		logger.debug( ((setFixed) ? "FIXED " : "") + " " + code + " in " + thingId);
    }
    
    
    /**
     * This is mainly to remove the curly braces 
     * that may cause an exception during 
     * MessageSource resolves the arguments.
     * 
     * @param args
     * @return
     */
    private String[] fixMessageArgs(Validation v, Object... args) {
    	String[] newArgs = new String[args.length];
    	int i=0;
    	for(Object a: args) {
    		if(a != null) {
    			String s = (a instanceof String) 
        			? (String)a : v.identify(a);
    			if (s.contains("{") || s.contains("}")) {
    				s.replaceAll("\\}", ")");
    				newArgs[i] = s.replaceAll("\\{", "(");
    			} else {
    				newArgs[i] = s;
    			}
    		} else {
    			newArgs[i] = "N/A";
    		}
    		i++;
    	}
    	return newArgs;
	}

}
