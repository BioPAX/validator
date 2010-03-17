package org.biopax.validator.impl;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.validator.Messenger;
import org.biopax.validator.Rule;
import org.biopax.validator.utils.BiopaxValidatorException;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Component;

@Component
@Configurable
public class SimplyThrowExceptionMessenger implements Messenger {
	   
    public void sendErrorCase(Rule rule, Object object, String code, Object... args) {
    	BiopaxValidatorException exception = 
    		(object instanceof BioPAXElement) 
    			? new BiopaxValidatorException((BioPAXElement)object, code, args)
    			: new BiopaxValidatorException(code, args);
    	
    	throw exception;
    }
    
}
