package org.biopax.validator.impl;

import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.core.annotation.Order;

import org.biopax.paxtools.io.simpleIO.SimpleReader;
import org.biopax.paxtools.io.simpleIO.SimpleReader.Triple;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.Named;
import org.biopax.validator.Behavior;
import org.biopax.validator.Rule;
import org.biopax.validator.result.ErrorType;
import org.biopax.validator.utils.BiopaxValidatorException;
import org.biopax.validator.utils.BiopaxValidatorUtils;

/**
 * This is the central aspect to report 
 * all the validation exceptions together with 
 * external exceptions that may happen in the PaxTools
 * and other libraries. 
 * 
 * This one must use Load-Time Weaving (LTW)
 * (checkit's configured in the META-INF/aop.xml, and 
 * JVM is started with -javaagent:spring-agent.jar option)!
 *
 * @author rodche
 */
@Configurable
@Aspect
@Order(50) // i.e., runs within the BehaviorAspect
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
    @Around("execution(void org.biopax.validator.Rule*+.check(*)) && args(thing)")
    public void adviseRuleExceptions(ProceedingJoinPoint jp, Object thing) {
    	// get the rule that checks now
    	Rule<?> rule = (Rule<?>) jp.getThis();
    	
    	if (log.isTraceEnabled()) log.trace(rule.getName() + " checks ");
    			
    	// get object's ID
    	String thingId = BiopaxValidatorUtils.getId(thing);
    	
    	if(thingId == null) {
    		if (log.isTraceEnabled()) log.trace("Id is not set yet; skipping.");
    		return;
    	}
    	
    	/*
    	 * Rule may throw exceptions
    	 * to be caught and reported 
    	 */
    	try {
    		
    		jp.proceed(); // go ahead validating the 'thing'
    		
    	} catch (Throwable t) {
   			log.error(rule.getName() + ".check(" + thingId 
    					+ ") caught the exception: " + t.toString(), t);
   			String msg = t.getMessage();
   			if(t instanceof BiopaxValidatorException) msg += "; " 
   				+ ((BiopaxValidatorException)t).getMsgArgs().toString();
   			ErrorType error = utils.createError(thingId, 
   					t.getClass().getSimpleName(), rule.getName(), Behavior.ERROR, msg);
    		report(thing, error);
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
    @Around("execution(void org.biopax.paxtools.io.simpleIO.SimpleReader.createAndBind(*)) " +
    		"&& args(model)")
    public void adviseCreateAndBind(ProceedingJoinPoint jp, Model model) {
    	SimpleReader reader = (SimpleReader) jp.getTarget();
    	String bpeId = null;
    	
    	// associate the model with the reader (and validation results)
    	validator.indirectlyAssociate(reader, model);
    	
        try {
            jp.proceed();
        } catch (Throwable ex) {
        	// try to get current element ID
        	try {
        		bpeId = reader.getId();
        	} catch (NullPointerException e) {
        		bpeId = reader.getXmlStreamInfo();
    		}
        	reportException(ex, reader, bpeId);
        }
    }
    
    
    @Around("execution(* org.biopax.paxtools.io.simpleIO.SimpleReader.processIndividual(*)) " 
    		+ "&& args(model)")
    public String adviseProcessIndividual(ProceedingJoinPoint jp, Model model) {  
    	String id = null;
    	SimpleReader reader = (SimpleReader) jp.getThis();
    	String bpeId = null;
    	try {
    		bpeId = reader.getId();
    	} catch (NullPointerException e) {
    		bpeId = reader.getXmlStreamInfo();
		}
    	
        try {
            id = (String) jp.proceed();
        } catch (Throwable ex) {
        	reportException(ex, reader, bpeId);
        }
        return id;
    }
    
    
    @Around("execution(private void org.biopax.paxtools.io.simpleIO.SimpleReader.bindValue(..))" +
    		" && args(triple,model)")
    public void adviseBindValue(ProceedingJoinPoint jp, Triple triple, Model model) {
    	SimpleReader reader = (SimpleReader) jp.getTarget();
    	// try to find better object to report with
    	Object o = reader;
    	BioPAXElement el = model.getByID(triple.domain);
    	if(el != null) {
    		o =  el;
    	} 
    	
        try {
            jp.proceed();
        } catch (Throwable t) {
        	reportException(t, o); //, triple);
        }
    }
    
    @Around("execution(* org.biopax.paxtools.controller.PropertyEditor*+.checkRestrictions(..)) " +
    		"&& args(bean, value)")
	public void adviseCheckRestrictions(ProceedingJoinPoint jp, Object bean, Object value) {
    	try {
    		jp.proceed();
    	} catch (Throwable ex) {
    		reportException(ex, bean, value);
    	}
	}    
    
    
    @Around("execution(* org.biopax.paxtools.controller.PropertyEditor*+.invokeMethod(..)) " +
    				"&& args(method, bean, value)")
	public void adviseInvokeMethod(ProceedingJoinPoint jp, Method method, Object bean, Object value) {
    	try {
    		jp.proceed();
    	} catch (Throwable ex) {
    		reportException(ex, bean, "value: " + value);
    	}
	}
    
    
    @Around("execution(* org.biopax.paxtools.io.BioPAXIOHandler*+.convertFromOWL(*))")
	public Object adviseConvertFromOwl(ProceedingJoinPoint jp) {
    	Object model = null;
    	try {
    		model = jp.proceed();
    	} catch (Throwable ex) {
    		reportException(ex, jp.getTarget()); // the second argument will be SimpleReader
    	}
    	
    	return model;
	}
    
    
    @Before("execution(* org.biopax.paxtools.io.simpleIO.SimpleReader.skip(..))")
	public void adviseUnknownClass(JoinPoint jp) {
    	SimpleReader reader = (SimpleReader) jp.getTarget();
    	String bpeId = null;
    	try {
    		bpeId = reader.getId();
    	} catch (NullPointerException e) {
    		bpeId = reader.getXmlStreamInfo();
		}
		ErrorType error = utils.createError(bpeId, "unknown.biopax.class", 
				"reader", Behavior.ERROR, reader.getXmlStreamInfo());
		report(reader, error);	
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
		if (log.isDebugEnabled()) {
			log.debug("duplicateNamesControl rule checks: " + bpe
					+ " gets name " + name);
		}
		if (bpe.getName().contains(name)) {
			ErrorType error = utils.createError(
				BiopaxValidatorUtils.getId(bpe), "duplicate.names",
					"duplicateNamesAdvice", Behavior.ERROR, name);
			report(bpe, error);
		}
	}

}
