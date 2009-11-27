package org.biopax.validator.impl;

import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.validator.Behavior;
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
@Order(10) // must be the highest priority among other aspects
public class BehaviorAspect {
    private static final Log logger  = LogFactory.getLog(BehaviorAspect.class);
    
    @Autowired
    private BiopaxValidatorUtils utils;
    
    @Resource
    private Set<String> parserIgnoredErrorCodes;
    
    @Around("execution(public void org.biopax.validator.Rule*+.check(*)) && args(thing)")
    public void checkBehavior(ProceedingJoinPoint jp, Object thing) throws Throwable {
    	
    	if(thing==null) return;
    	
    	Rule<?> r = (Rule<?>) jp.getTarget();
    	if (logger.isTraceEnabled()) {
    		String what = (thing instanceof BioPAXElement) ? 
    				((BioPAXElement)thing).getRDFId() : thing.toString();
            logger.trace(r.getName() + " (" + r.getBehavior() + ") checks " + what);
        }
        if (!Behavior.IGNORE.equals(r.getBehavior())) {
        	jp.proceed();
        } else {
            if (logger.isTraceEnabled()) {
                logger.trace("skipped");
            }
        }
    }

    @Around("execution(void org.biopax.validator.Rule*+.fix(..))")
    public void checkFixBehavior(ProceedingJoinPoint jp) throws Throwable {
    	Rule<?> r = (Rule<?>) jp.getTarget();
        if (Behavior.FIXIT.equals(r.getBehavior())) {
        	if (logger.isTraceEnabled()) {
                logger.trace(r.getName() + " is fixing something");
            }
           	jp.proceed();
        }
    }
       
    /**
     * @deprecated this is to debug; better use rule's postModelOnly property
     * 
     * Temporarily sets the list of error codes
     * to ignore during the model is being read.
     * 
     * @param jp
     * @return
     * @throws Throwable
     */
    @Around("execution(* org.biopax.validator.Validator*+.importModel(..))")
	public Object setParserIgnoredCodes(ProceedingJoinPoint jp) throws Throwable {
		utils.addIgnoredCodes(parserIgnoredErrorCodes);
		Object o = jp.proceed();
		utils.removeIgnoredCodes(parserIgnoredErrorCodes);
		return o;
	}
    
}

