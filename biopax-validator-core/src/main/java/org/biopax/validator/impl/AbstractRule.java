package org.biopax.validator.impl;

import org.biopax.validator.Rule;
import org.biopax.validator.result.ErrorType;
import org.biopax.validator.result.Validation;
import org.biopax.validator.utils.BiopaxValidatorUtils;

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
    private BiopaxValidatorUtils utils;
    
    
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
    	String thingId = BiopaxValidatorUtils.getId(object);
  	    	
    	// create and add/update the error case using current validation profile
    	ErrorType error = (utils != null) 
    			? utils.createError(thingId, code, getClass().getName(), validation.getProfile(), setFixed, args)
    			// when - no config. available (JUnit tests?); it will be 'ERROR' type with default messages:
    			: BiopaxValidatorUtils.createError(null, null,
    				thingId, code, getClass().getName(), null, setFixed, args);
    	
    	validation.addError(error);

    	if(logger.isDebugEnabled())
    		logger.debug( ((setFixed) ? "FIXED " : "") + " " + code + " in " + thingId);
    }

}
