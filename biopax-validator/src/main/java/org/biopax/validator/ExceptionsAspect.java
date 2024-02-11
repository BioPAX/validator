package org.biopax.validator;


import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.biopax.validator.api.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.biopax.paxtools.controller.PropertyEditor;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.io.SimpleIOHandler.Triple;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.validator.api.AbstractAspect;


/**
 * This is the central aspect to report 
 * all the validation exceptions together with 
 * external exceptions that may happen in the PaxTools
 * and other libraries. 
 *
 * @author rodche
 */
@Configurable
@Aspect
public class ExceptionsAspect extends AbstractAspect {

	private static final Logger log = LoggerFactory.getLogger(ExceptionsAspect.class);

  @Autowired
	@Override
  public void setValidator(Validator biopaxValidator) {
    this.validator = biopaxValidator;
  }

	/**
	 * This captures the exceptions that occur
	 * during the model build and, more important,
	 * associates the just created model
	 * with the corresponding validation result
	 * (this is the earliest possibility to do so)!
	 *
	 * @param jp (AspectJ) joint point
	 * @param model biopax model
	 */
	@Around("execution(void org.biopax.paxtools.io.SimpleIOHandler+.createAndBind(*)) " +
		"&& args(model)")
	public Object adviseCreateAndBind(ProceedingJoinPoint jp, Model model) {
		SimpleIOHandler reader = (SimpleIOHandler) jp.getTarget();
		// associate the model with the reader (and validation results)
		getValidator().indirectlyAssociate(reader, model);

		Object ret = null;
		try {
			ret = jp.proceed();
		} catch (Throwable ex) {
			reportException(ex, reader, "syntax.error", "SimpleIOHandler.createAndBind interceptor", null);
		}

		return ret;
	}

	@Around("execution(* org.biopax.paxtools.io.SimpleIOHandler+.processIndividual(*)) "
		+ "&& args(model)")
	public String adviseProcessIndividual(ProceedingJoinPoint jp, Model model) {
		String id = null;
		SimpleIOHandler reader = (SimpleIOHandler) jp.getThis();

		try {
			id = (String) jp.proceed();
		} catch (Throwable ex) {
			reportException(ex, reader, "syntax.error", "SimpleIOHandler.processIndividual interceptor", null);
		}
		return id;
	}

	@Around("execution(private void org.biopax.paxtools.io.SimpleIOHandler+.bindValue(..))" +
		" && args(triple, model)")
	public Object adviseBindValue(ProceedingJoinPoint jp, Triple triple, Model model) {
		if(log.isDebugEnabled())
			log.debug("adviseBindValue, triple: " + triple);

		SimpleIOHandler reader = (SimpleIOHandler) jp.getTarget();

		// try to find the best object to report about...
		if(triple.domain==null || triple.property==null) {
			report(reader, "syntax.error",
				"SimpleIOHandler.bindValue interceptor", false, triple +	" - skipped");
		} else {
			BioPAXElement el = model.getByID(triple.domain);
			if (el != null) {
				PropertyEditor<?, ?> editor = reader.getEditorMap()
					.getEditorForProperty(triple.property, el.getModelInterface());
				if (editor == null) {
					// auto-fix (for some)
					if (triple.property != null && triple.property.equals("taxonXref")) {
						report(el, "unknown.property",
							"SimpleIOHandler.bindValue interceptor",
							true, triple.property + " - replaced with 'xref'");
						triple.property = "xref";
					} else {
						report(el, "unknown.property",
							"SimpleIOHandler.bindValue interceptor",
							false, triple.property + " - skipped");
					}
				}
			}
		}

    Object ret = null;
		try {
			ret = jp.proceed();
		} catch (Throwable t) {
			reportException(t, reader, "syntax.error", "SimpleIOHandler.bindValue interceptor",
        String.valueOf(triple));
		}

		return ret;
	}

	@Around("execution(protected void org.biopax.paxtools.controller.PropertyEditor*+.checkRestrictions(..)) " +
		"&& args(value, bean)")
	public void adviseCheckRestrictions(ProceedingJoinPoint jp,  Object value, BioPAXElement bean) {
		try {
			jp.proceed();
		} catch (Throwable ex) {
			reportException(ex, bean, "syntax.error", "PropertyEditor.checkRestrictions interceptor",
        String.valueOf(value));
		}
	}

	@Around("execution(protected void org.biopax.paxtools.controller.PropertyEditor*+.invokeMethod(..)) " +
		"&& args(method, bean, value)")
	public Object adviseInvokeMethod(ProceedingJoinPoint jp, Method method, BioPAXElement bean, Object value) {
		Object ret = null;
	  try {
			ret = jp.proceed();
		} catch (Throwable ex) {
			reportException(ex, bean, "syntax.error", "PropertyEditor.invokeMethod interceptor",
        "method: "+ method + ", value: " + value);
		}

		return ret;
	}

	@Around("execution(* org.biopax.paxtools.io.BioPAXIOHandler*+.convertFromOWL(*))")
	public Object adviseConvertFromOwl(ProceedingJoinPoint jp) {
		Object model = null;
		try {
			model = jp.proceed();
		} catch (Throwable ex) {
			reportException(ex, jp.getTarget(), "syntax.error", "BioPAXIOHandler.convertFromOWL interceptor", null);
		}

		return model;
	}

	@Before("execution(* org.biopax.paxtools.io.SimpleIOHandler+.skip(..))")
	public void adviseUnknownClass(JoinPoint jp) {
		SimpleIOHandler reader = (SimpleIOHandler) jp.getTarget();
		String loc = reader.getXmlStreamInfo();
		report(reader, "unknown.class", "SimpleIOHandler.skip interceptor", false, loc);
	}

}
