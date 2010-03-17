package org.biopax.validator.impl;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.validator.Rule;
import org.biopax.validator.utils.BiopaxValidatorException;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Component;

@Component
@Configurable
public class Messenger {
	   
    /**
     * This is a precious piece of code about validation errors,
     * which is not only throws the exception (which is good for debugging),
     * but also is intercepted by the AOP advice that makes use of this 
     * method and its arguments to actually register the error case!
     * 
     * @param object that is invalid or caused the error
     * @param code error code, e.g., 'illegal.value'
     * @param args extra parameters for the error message template
     */
    public void sendErrorCase(Rule rule, Object object, String code, Object... args) {
    	BiopaxValidatorException exception = 
    		(object instanceof BioPAXElement) 
    			? new BiopaxValidatorException((BioPAXElement)object, code, args)
    			: new BiopaxValidatorException(code, args);
    	
    	throw exception;
    }
    
}
