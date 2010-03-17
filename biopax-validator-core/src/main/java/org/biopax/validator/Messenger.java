package org.biopax.validator;

public interface Messenger {
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
	<T> void sendErrorCase(Rule<T> rule, Object object, String code, Object... args);
}
