package org.biopax.validator.impl;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.validator.Messenger;
import org.biopax.validator.Rule;
import org.biopax.validator.utils.BiopaxValidatorException;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Component;

/**
 * Always throws a new BiopaxValidatorException
 * (to be registered and/or suppressed by, e.g., AOP advice...)
 * 
 * @author rodche
 *
 */
@Component
@Configurable
public class SimplyThrowExceptionMessenger implements Messenger {
	   
    public void sendErrorCase(Rule rule, Object object, String code, 
    		boolean setFixed, Object... args) 
    {	
    		String msg = ((setFixed) ? "FIXED " : "") 
    			+ rule.getBehavior().toString() + " " + code;
    		
    		BiopaxValidatorException exception = (object instanceof BioPAXElement) 
    			? new BiopaxValidatorException((BioPAXElement)object, msg, args)
    			: new BiopaxValidatorException(msg, args);
    	
    			throw exception;
    }
    
}
