package org.biopax.validator.utils;

/**
 * A validation runtime exception.
 *
 * @author rodche
 */
public class BiopaxValidatorException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	
	private Object[] msgArgs;
	
    public BiopaxValidatorException(String msg) {
        super(msg);
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
    
    public Object[] getMsgArgs() {
		return msgArgs;
	}
	
}