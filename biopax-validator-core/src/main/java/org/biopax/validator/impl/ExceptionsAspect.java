package org.biopax.validator.impl;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.Arrays;

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
import org.biopax.paxtools.model.level3.Named;
import org.biopax.validator.result.Behavior;
import org.biopax.validator.Rule;
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
 * JVM is started with -javaagent:spring-instrument.jar option)!
 *
 * @author rodche
 */
//@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Configurable
@Aspect
@Order(50)
public class ExceptionsAspect extends AbstractAspect {
	private static final Log log = LogFactory.getLog(ExceptionsAspect.class);

	/**
	 * Registers all the BioPAX errors and warnings 
	 * found by the validation rules. 
	 *
	 * The BioPAX element RDFId and rule name
	 * are reported along with error cases.
	 * 
	 * @param jp AOP Proceeding Joint Point
	 * @param rule validation rule that found the error case
	 * @param thing object associated with the error case
	 * @param code error code
	 * @param args extra parameters of the error message
	 * @throws Throwable
	 */
    @Around("execution(void org.biopax.validator.Messenger*+.sendErrorCase(..)) && args(rule, thing, code, fixed, args)")
    public void adviseSendErrorCase(ProceedingJoinPoint jp, Rule<?> rule, Object thing,
    		String code, boolean fixed, Object... args) throws Throwable 
    {    	    	
    	assert(thing != null); // works when assertions are enabled (-ea JVM opt.)
    	
    	String ruleName = rule.getName();
    	
    	if (log.isTraceEnabled()) 
    		log.trace("advising sendErrorCase called by " + ruleName 
    			+ " that found a problem : " + code);
    			
    	// get object's ID
    	String thingId = BiopaxValidatorUtils.getId(thing);
    	
    	if(thing == null) {
    		if (log.isWarnEnabled()) 
    			log.warn("The 'thing' (the error is about) is NULL! Skipping.");
    		return;
    	}
    	
    	if(thing == null || thingId == null) {
    		if (log.isTraceEnabled()) 
    			log.trace("RDFId is not set yet; skipping.");
    		return;
    	}
    		
    	report(thing, thingId, code, ruleName, rule.getBehavior(), fixed, args);
    		
		try {
			jp.proceed(); // in fact, dummy
		} catch (BiopaxValidatorException e) {
			// just ignore the (above processed) exception
		}
    }
	
	
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
    @Around("execution(public void org.biopax.validator.Rule*+.check(..)) && args(thing, fix)")
    public void adviseRuleExceptions(ProceedingJoinPoint jp, Object thing, boolean fix) {
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
    	 * Rule may throw other exceptions
    	 * to be caught and reported accurately 
    	 * to fix what's probably a bug
    	 */
    	try {
    		jp.proceed(); // go ahead validating the 'thing'
    	} catch (Throwable t) {
   			log.fatal(rule.getName() + ".check(" + thingId 
    			+ ") threw the exception: " + t.toString(), t);
   			String msg = t.toString() + " - " + getStackTrace(t) + " - ";
   			if(t instanceof BiopaxValidatorException)
   				msg = msg + Arrays.toString(((BiopaxValidatorException)t).getMsgArgs());
   			
    		// report the exception using rule's behavior mode
   			report(thing, thingId, t.getClass().getSimpleName(), 
    				rule.getName(), rule.getBehavior(), false, msg);
    	}
    }
    
    private static String getStackTrace(Throwable aThrowable) {
        final PrintWriter printWriter = new PrintWriter(new StringWriter());
        aThrowable.printStackTrace(printWriter);
        return printWriter.toString();
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
    
    
    @Around("execution(* org.biopax.paxtools.io.SimpleIOHandler.processIndividual(*)) " 
    		+ "&& args(model)")
    public String adviseProcessIndividual(ProceedingJoinPoint jp, Model model) {  
    	String id = null;
    	SimpleIOHandler reader = (SimpleIOHandler) jp.getThis();
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
    
    
    @Around("execution(private void org.biopax.paxtools.io.SimpleIOHandler.bindValue(..))" +
    		" && args(triple, model)")
    public void adviseBindValue(ProceedingJoinPoint jp, Triple triple, Model model) {
    	SimpleIOHandler reader = (SimpleIOHandler) jp.getTarget();
    	// try to find the best object to report about...
    	Object o = reader;
    	BioPAXElement el = model.getByID(triple.domain);
    	if(el != null) {
    		o =  el;
			PropertyEditor editor = reader.getEditorMap()
				.getEditorForProperty(triple.property, el.getModelInterface());
			if (editor == null) {
				// auto-fix (for some)
				if(triple.property.equals("taxonXref")) {
					report(el, BiopaxValidatorUtils.getId(el), "unknown.property", 
							"reader", Behavior.ERROR, true, triple.property + 
							" - replaced with 'xref'");
					triple.property = "xref";
				} else {
					report(el, BiopaxValidatorUtils.getId(el), "unknown.property", 
							"reader", Behavior.ERROR, false, triple.property + 
							" - skipped");
				}
			}
    	} 
    	    	
		try {
			jp.proceed();
		} catch (Throwable t) {
			reportException(t, o); // , triple);
		}
    }
    
    @Around("execution(protected void org.biopax.paxtools.controller.PropertyEditor*+.checkRestrictions(..)) " +
    		"&& args(value, bean)")
	public void adviseCheckRestrictions(ProceedingJoinPoint jp,  Object value, BioPAXElement bean) {
    	try {
    		jp.proceed();
    	} catch (Throwable ex) {
    		reportException(ex, bean, value);
    	}
	}    
    
    
    @Around("execution(protected void org.biopax.paxtools.controller.PropertyEditor*+.invokeMethod(..)) " +
    				"&& args(method, bean, value)")
	public void adviseInvokeMethod(ProceedingJoinPoint jp, Method method, BioPAXElement bean, Object value) {
    	try {
    		jp.proceed();
    	} catch (Throwable ex) {
    		reportException(ex, bean, "method: "+ method + ", value: " + value);
    	}
	}
    
    
    @Around("execution(* org.biopax.paxtools.io.BioPAXIOHandler*+.convertFromOWL(*))")
	public Object adviseConvertFromOwl(ProceedingJoinPoint jp) {
    	Object model = null;
    	try {
    		model = jp.proceed();
    	} catch (Throwable ex) {
    		reportException(ex, jp.getTarget()); // the second argument will be SimpleIOHandler
    	}
    	
    	return model;
	}
    
    
    @Before("execution(* org.biopax.paxtools.io.SimpleIOHandler.skip(..))")
	public void adviseUnknownClass(JoinPoint jp) {
    	SimpleIOHandler reader = (SimpleIOHandler) jp.getTarget();
    	String loc = reader.getXmlStreamInfo();
		report(reader, loc, "unknown.class", 
			"reader", Behavior.ERROR, false, loc);	
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
			report(bpe, BiopaxValidatorUtils.getId(bpe), "duplicate.names",
					"duplicateNamesAdvice", Behavior.WARNING, true, name);
		}
	}
	
}
