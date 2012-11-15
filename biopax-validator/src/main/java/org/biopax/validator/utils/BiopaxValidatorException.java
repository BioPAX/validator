package org.biopax.validator.utils;

import org.biopax.paxtools.model.BioPAXElement;

/**
 * A validation runtime exception.
 *
 * @author rodche
 */
public class BiopaxValidatorException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	
	private BioPAXElement element;
	private Object[] msgArgs;
	
    public BiopaxValidatorException(String msg) {
        super(msg); // msg is usually the BioPAX "error code"
        msgArgs = new Object[]{};
    }

    public BiopaxValidatorException(String msg, Object... args) {
    	super(msg);
        this.msgArgs = args;
    }
    
    public BiopaxValidatorException(Throwable t, Object... args) {
        super(t);
        this.msgArgs = args;
    }
    
    public BiopaxValidatorException(BioPAXElement element, String msg, Object... args) {
    	super(msg);
        this.msgArgs = args;
        this.element = element;
    }
    
    public BiopaxValidatorException(BioPAXElement element, Throwable t, Object... args) {
        super(t);
        this.msgArgs = args;
        this.element = element;
    }
    
    public Object[] getMsgArgs() {
		return msgArgs;
	}
	
    public BioPAXElement getElement() {
		return element;
	}
    
}