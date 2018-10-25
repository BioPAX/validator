package org.biopax.validator.api;

import java.util.Arrays;

/**
 * A validation runtime exception.
 *
 * @author rodche
 */
public class ValidatorException extends RuntimeException {
	private static final long serialVersionUID = 1L;

  private final Object[] msgArgs;

    public ValidatorException(String msg, Object... args) {
    	super(msg);
      msgArgs = args;
    }
    
    public ValidatorException(Throwable t, Object... args) {
      super(t);
      msgArgs = args;
    }
    
    public Object[] getMsgArgs() {
		return msgArgs;
	}

    @Override
    public String toString() {
    	return super.toString() + Arrays.toString(msgArgs);
    }
    
}