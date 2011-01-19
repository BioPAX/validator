package org.biopax.validator.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.validator.result.Behavior;
import org.biopax.validator.Rule;
import org.biopax.validator.utils.BiopaxValidatorUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.core.annotation.Order;

/**
 * This is a behavior aspect to let go or skip
 * other object's action, depending on its 
 * current "behavior" and "enabled" values. 
 * This also logs all the unexpected exceptions 
 * that may occur during method execution.
 *
 * @author rodche
 */
@Configurable
@Aspect
@Order(25) // 'checkBehavior' runs around the ExceptionsAspect's 'adviseRuleExceptions'!
public class BehaviorAspect extends AbstractAspect {
    private static final Log logger  = LogFactory.getLog(BehaviorAspect.class);
    
    @Autowired
    private BiopaxValidatorUtils utils;
    
    @Around("execution(public void org.biopax.validator.Rule*+.check(..)) && args(thing, fix)")
    public void checkBehavior(ProceedingJoinPoint jp, Object thing, boolean fix) 
    	throws Throwable 
    {	
    	if(thing==null) return;
    	
    	Rule<?> r = (Rule<?>) jp.getTarget();
    	if (logger.isTraceEnabled()) {
    		String what = (thing instanceof BioPAXElement) ? 
    				((BioPAXElement)thing).getRDFId() : thing.toString();
            logger.trace(r.getName() + " (" + r.getBehavior() + ") checks " + what);
        }
        if (r.getBehavior() != Behavior.IGNORE) {
        	jp.proceed();
        } else {
            if (logger.isTraceEnabled()) {
                logger.trace("skipped");
            }
        }
    }
    
}

