package org.biopax.validator.api;

/**
 * A validation runtime exception.
 *
 * @author rodche
 */
public class ValidatorException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	
	private final Object element;
	private final Object[] msgArgs;

    public ValidatorException(String msg, Object... args) {
    	super(msg);
    	element = null;
        this.msgArgs = args;
    }
    
    public ValidatorException(Throwable t, Object... args) {
        super(t);
        element = null;
        this.msgArgs = args;
    }
    
    public Object[] getMsgArgs() {
		return msgArgs;
	}
	
    public Object getElement() {
		return element;
	}
    
    @Override
    public String toString() {
    	return super.toString() + msgArgs.toString();
    }
    
}