package org.biopax.validator;


/**
 * Validation rule interface.
 * 
 * Although at run time T is a BioPAXElement
 * (this is 100% true for the current version),
 * at compile time, when implementing a rule, one may
 * prefer to use other interfaces as well, e.g.,
 * Process or Named. Therefore, "T extends BioPAXElement"
 * is not used here.
 *
 * @author rodche
 */
public interface Rule<T> {  

	/**
     * Validates the object.
     * 
     * @param thing to validate
     */
	void check(T thing);

    /**
     * Can check it?
     *
     * @param thing
     * @return True when it can.
     */
    boolean canCheck(Object thing);
    
    /**
     * 
     * @return tip/description
     */
    String getTip();
    
    /**
     * 
     * @return Rule's (bean) name
     */
    String getName();
    
    
    /**
     * Rule's behavior.  
     * 
     * This is not only for logging, but also defines 
     * the action it takes when reports errors. 
     * For instance, 'FAIL' could tell the exception to pop up, 
     * 'ERROR' - log as 'error' and continue, 
     * 'IGNORE' - do not check or even mention it, etc.
     * 
     * @return current Behavior
     */
    Behavior getBehavior();

    /**
     * Sets behavior property for the rule.
     * 
     * @param behavior
     */
    void setBehavior(Behavior behavior);
    
    /**
     * A "post-model" rule should not  
     * check every time the object T or 
     * a reference to that is modified.
     * It is designed to check either after 
     * the model is built or several 
     * related modifications are complete.
     * 
     * @return boolean
     */
    boolean isPostModelOnly();
       
    
    /**
     * Reports errors
     * 
     * @param object where the problem is
     * @param code error 'code'
     * @param args optional arguments for the error message
     */
    void error(Object object, String code, Object... args);
}
