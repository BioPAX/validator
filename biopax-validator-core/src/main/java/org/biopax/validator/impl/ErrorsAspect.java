package org.biopax.validator.impl;

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.core.annotation.Order;

import org.biopax.paxtools.model.level3.Named;
import org.biopax.validator.Behavior;
import org.biopax.validator.Rule;
import org.biopax.validator.result.ErrorType;
import org.biopax.validator.result.Validation;
import org.biopax.validator.utils.BiopaxValidatorException;
import org.biopax.validator.utils.BiopaxValidatorUtils;

/**
 * This is the central aspect to report 
 * the BioPAX syntax and semantic errors.
 *
 * @author rodche
 */
@Configurable
@Aspect
@Order(30) // Plays inside the BehaviorAspect and ExceptionsAspect!
public class ErrorsAspect extends AbstractAspect {
	private static final Log log = LogFactory.getLog(ErrorsAspect.class);
	
	/**
	 * Registers all the BioPAX errors and warnings 
	 * found by the validation rules. 
	 *
	 * The BioPAX element RDFId and rule name
	 * are reported along with error cases.
	 * 
	 * @param jp AOP Proceeding Joint Point
	 * @param thing a BioPAX element
	 * @throws Throwable 
	 */
    @Around("execution(void org.biopax.validator.impl.AbstractRule+.error(..)) " +
    		"&& args(thing, code, args)")
    public void adviseRuleError(ProceedingJoinPoint jp, Object thing, 
    		String code, Object... args) throws Throwable {
    	// get the rule that checks now
    	Rule<?> rule = (Rule<?>) jp.getThis();
    	
    	if (log.isTraceEnabled()) log.trace(rule.getName() + " found a problem");
    			
    	// get object's ID
    	String thingId = BiopaxValidatorUtils.getId(thing);
    	
    	if(thingId == null) {
    		if (log.isTraceEnabled()) log.trace("RDFId is not set yet; skipping.");
    		return;
    	}
    	
    	Behavior errOrWarn = (rule.getBehavior() == Behavior.WARNING) 
    		? Behavior.WARNING : Behavior.ERROR;
    	
    	// find the keys (of reports) for the models that contain this element
    	Collection<Validation> keys = validator.findKey(thing);
    	
    	ErrorType error = utils.createError(thingId, code, 
				rule.getName(), errOrWarn, args);			
		report(keys, error);
    	
		try {
			jp.proceed(); // in fact, dummy
		} catch (BiopaxValidatorException e) {
			// nothing to do
		}
    }

	
	/**
	 * Duplicate names syntax rule: 
	 * if a value is set either for the standardName 
	 * or displayName, adding it to the name is not necessary
	 * (best practices)
	 * 
	 * @param jp
	 * @param name
	 */
	@Before("target(org.biopax.paxtools.model.level3.Named) " +
			"&& ( execution(public void addName(*)) || execution(public void set*(*)) ) " +
			"&& args(name)")
	public void adviseAddName(JoinPoint jp, String name) {
		Named bpe = (Named) jp.getTarget();
		if(bpe.getName().contains(name)) {
			// find the keys (of reports) for the models that contain this element
	    	Collection<Validation> keys = validator.findKey(bpe);
	    	ErrorType error = utils.createError(BiopaxValidatorUtils.getId(bpe), 
	    			"duplicate.names", "controlAspect", Behavior.ERROR,
	    			name);			
			report(keys, error);
		}
	}
    
}
