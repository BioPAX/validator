package org.biopax.validator.impl;

import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.core.annotation.Order;

import org.biopax.paxtools.controller.PropertyEditor;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.io.SimpleIOHandler.Triple;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.validator.Rule;
import org.biopax.validator.result.Validation;
import org.biopax.validator.utils.BiopaxValidatorUtils;

/**
 * This is the central aspect to report 
 * all the validation exceptions together with 
 * external exceptions that may happen in the PaxTools
 * and other libraries. 
 * 
 * This one must use Load-Time Weaving (LTW)
 * (checkit's configured in the META-INF/aop.xml, and 
 * JVM is started with -javaagent:spring-instrument.jar option)!
 *
 * @author rodche
 */
@Configurable
@Aspect
@Order(50)
public class ExceptionsAspect extends AbstractAspect {
	private static final Log log = LogFactory.getLog(ExceptionsAspect.class);
	
	/**
	 * This is the central method to register 
	 * any "UNEXPECTED" problems with the 
	 * appropriate validation result.
	 * 
	 * The BioPAX element RDFId and rule name
	 * are reported along with error cases.
	 * 
	 * @param jp AOP Proceeding Joint Point
	 * @param thing a BioPAX element
	 * @throws Throwable
	 */
    @Around("execution(public void org.biopax.validator.Rule*+.check(..)) && args(validation, thing, fix)")
    public void adviseRuleExceptions(ProceedingJoinPoint jp, Validation validation, Object thing, boolean fix) {
    	// get the rule that checks now
    	Rule<?> rule = (Rule<?>) jp.getThis();
    			
    	// ?
    	String thingId = BiopaxValidatorUtils.getId(thing);
    	if(thingId == null) {
    		log.warn("Id is not set yet; skipping.");
//    		return;
    	}
    	
    	/*
    	 * Rule may eventually throw other exceptions
    	 * (e.g., due to yet unknown bugs).
    	 * These are to be caught and reported accurately 
    	 * by the validation framework too.
    	 */
    	try {
    		jp.proceed(); // go ahead validating the 'thing'
    	} catch (Throwable t) {
   			log.fatal(rule + ".check(" + thingId 
    			+ ") threw the exception: " + t.toString(), t);
   			
   			reportException(t, thing, "exception", rule.getClass().getName() + ".check interceptor");
    	}
    }
    
    
    /**
     * This captures the exceptions that occur 
     * during the model build and, more important,
     * associates the just created model
     * with the corresponding validation result
     * (this is the earliest possibility to do so)!
     * 
     * @param jp
     */
    @Around("execution(void org.biopax.paxtools.io.SimpleIOHandler.createAndBind(*)) " +
    		"&& args(model)")
    public void adviseCreateAndBind(ProceedingJoinPoint jp, Model model) {
    	SimpleIOHandler reader = (SimpleIOHandler) jp.getTarget();
    	
    	// associate the model with the reader (and validation results)
    	validator.indirectlyAssociate(reader, model);
    	
        try {
            jp.proceed();
        } catch (Throwable ex) {
        	reportException(ex, reader, "syntax.error", "SimpleIOHandler.createAndBind interceptor");
        }
    }
    
    
    @Around("execution(* org.biopax.paxtools.io.SimpleIOHandler.processIndividual(*)) " 
    		+ "&& args(model)")
    public String adviseProcessIndividual(ProceedingJoinPoint jp, Model model) {  
    	String id = null;
    	SimpleIOHandler reader = (SimpleIOHandler) jp.getThis();
    	
        try {
            id = (String) jp.proceed();
        } catch (Throwable ex) {
        	reportException(ex, reader, "syntax.error", "SimpleIOHandler.processIndividual interceptor");
        }
        return id;
    }
    
    
    @Around("execution(private void org.biopax.paxtools.io.SimpleIOHandler.bindValue(..))" +
    		" && args(triple, model)")
    public void adviseBindValue(ProceedingJoinPoint jp, Triple triple, Model model) {
    	SimpleIOHandler reader = (SimpleIOHandler) jp.getTarget();
    	// try to find the best object to report about...
    	Object o = reader;
    	BioPAXElement el = model.getByID(triple.domain);
    	if(el != null) {
    		o =  el;
			PropertyEditor<?,?> editor = reader.getEditorMap()
				.getEditorForProperty(triple.property, el.getModelInterface());
			if (editor == null) {
				// auto-fix (for some)
				if(triple.property.equals("taxonXref")) {
					report(el, "unknown.property", 
							"SimpleIOHandler.bindValue interceptor", 
							true, triple.property + 
							" - replaced with 'xref'");
					triple.property = "xref";
				} else {
					report(el, "unknown.property", 
							"SimpleIOHandler.bindValue interceptor", 
							false, triple.property + 
							" - skipped");
				}
			}
    	} 
    	    	
		try {
			jp.proceed();
		} catch (Throwable t) {
			reportException(t, o, "syntax.error", "SimpleIOHandler.bindValue interceptor"); // , triple);
		}
    }
    
    @Around("execution(protected void org.biopax.paxtools.controller.PropertyEditor*+.checkRestrictions(..)) " +
    		"&& args(value, bean)")
	public void adviseCheckRestrictions(ProceedingJoinPoint jp,  Object value, BioPAXElement bean) {
    	try {
    		jp.proceed();
    	} catch (Throwable ex) {
    		reportException(ex, bean, "syntax.error", "PropertyEditor.checkRestrictions interceptor", value);
    	}
	}    
    
    
    @Around("execution(protected void org.biopax.paxtools.controller.PropertyEditor*+.invokeMethod(..)) " +
    				"&& args(method, bean, value)")
	public void adviseInvokeMethod(ProceedingJoinPoint jp, Method method, BioPAXElement bean, Object value) {
    	try {
    		jp.proceed();
    	} catch (Throwable ex) {
    		reportException(ex, bean, "syntax.error", "PropertyEditor.invokeMethod interceptor", "method: "+ method + ", value: " + value);
    	}
	}
    
    
    @Around("execution(* org.biopax.paxtools.io.BioPAXIOHandler*+.convertFromOWL(*))")
	public Object adviseConvertFromOwl(ProceedingJoinPoint jp) {
    	Object model = null;
    	try {
    		model = jp.proceed();
    	} catch (Throwable ex) {
    		reportException(ex, jp.getTarget(), "syntax.error", "BioPAXIOHandler.convertFromOWL interceptor"); // the second argument will be SimpleIOHandler
    	}
    	
    	return model;
	}
    
    
    @Before("execution(* org.biopax.paxtools.io.SimpleIOHandler.skip(..))")
	public void adviseUnknownClass(JoinPoint jp) {
    	SimpleIOHandler reader = (SimpleIOHandler) jp.getTarget();
    	String loc = reader.getXmlStreamInfo();
		report(reader, "unknown.class", "SimpleIOHandler.skip interceptor", false, loc);	
	}
	
}
