package org.biopax.validator.impl;

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.validator.Rule;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * This is a BioPAX Model controller/validator.
 *
 * It checks all qualified rules for each individual 
 * BioPAX element before and after it's modified.
 * 
 * This AOP aspect must use Load-Time Weaving (LTW)
 * (checkit's configured in the META-INF/aop.xml, and 
 * JVM is started with -javaagent:spring-agent.jar option)!
 *
 * @author rodche
 */
@Configurable
@Aspect
public class ControlAspect extends AbstractAspect { 
	private static final Log log = LogFactory.getLog(ControlAspect.class);
	
	@Before("execution(void org.biopax.paxtools.model.BioPAXElement*+.set*(*))"
			+ " && args(value)")
	public void adviseSetProperty(JoinPoint jp, Object value) {
		checkElementOrSet(jp.getTarget(), value);
	}

	@Before("execution(void org.biopax.paxtools.model.BioPAXElement*+.add*(*))"
			+ " && args(value)")
	public void adviseAddProperty(JoinPoint jp, Object value) {
		checkElementOrSet(jp.getTarget(), value);
	}

	@Before("execution(void org.biopax.paxtools.model.Model*+.add(*))"
			+ " && args(value)")
	public void adviseAddElementToModel(JoinPoint jp, BioPAXElement value) {
		checkElementOrSet(jp.getTarget(), value);
	}
	
	@After("execution(void org.biopax.paxtools.model.BioPAXElement*+.set*(*))")
	public void adviseAfterSetProperty(JoinPoint jp) {
		checkElementOrSet(null, jp.getTarget());
	}

	@After("execution(void org.biopax.paxtools.model.BioPAXElement*+.add*(*))")
	public void adviseAfterAddProperty(JoinPoint jp) {
		checkElementOrSet(null, jp.getTarget());
	}
    		
	/**
	 * Checks an object and reports to
	 * the global "errors" bean (getErrors()).
	 * 
	 * This is for AOP checks ("fast" validation).
	 * 
	 * Normally (in the current implementation) 
	 * rules cannot check/report anything when 
	 * the object is of a primitive type
	 * (i.e. it is the value of a BioPAX element 
	 * Data Type Property)
	 * 
	 * @param parent BioPAX element or Model
	 * @param value BioPAX property value (a set, element, or primitive value)
	 */
	private void checkElementOrSet(Object parent, Object value) {	
		if (value instanceof Collection) {
			for (Object obj : (Collection) value) {
				checkElement(parent, obj);
			}
		} else {
			checkElement(parent, value);
		}
	}
	
	/**
	 * Checks rues that can do about this object
	 * (when the value is not a collection.)
	 * 
	 * @param parent parent element or model
	 * @param value to check
	 */
	@SuppressWarnings("unchecked")
	private void checkElement(Object parent, Object value) {
		if (! (value instanceof BioPAXElement) ) {
			/* 
			 * Skipping primitive types, Enum, and String values 
			 * is not a big deal (limitation), because
			 * they are better check using a rule for the corresponding 
			 * parent BioPAX element.
			 */
			if(log.isDebugEnabled()) {
				log.debug("Skipping check 'data type' value : " + value 
						+ " of element " + parent 
						+ " before it's assigned (will do after)");	
			}
			
			return;
		} 
		
		/* trying not to lost any 'intermediate' errors, we
		 temporarily associate the 'value' with a validation result 
		 through its 'parent' (element or model);
		 there is no effect if 'parent' is null though.
		 */
		validator.indirectlyAssociate(parent, value);
		
		// check all rules that can do this element
		for (Rule rule : validator.getRules()) {
			if (!rule.isPostModelOnly() && rule.canCheck(value)) {
				rule.check(value);
			}
		}
		
		/* break the temporary association 
		 (but if the BioPAX element (value) has been already part of a model,
		 it is still associated with the model's Validation result anyway)
		*/
		validator.freeObject(value);

		if(log.isTraceEnabled()) {
			log.trace("Validator bean: " + validator);
		}
		
	}
	
}

