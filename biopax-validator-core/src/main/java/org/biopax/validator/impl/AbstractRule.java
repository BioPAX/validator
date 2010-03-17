package org.biopax.validator.impl;

import org.biopax.validator.Behavior;
import org.biopax.validator.Rule;
import org.biopax.validator.utils.BiopaxValidatorException;
import org.biopax.validator.utils.BiopaxValidatorUtils;

import java.util.Locale;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.MessageSource;

/**
 * Abstract validation rule.
 * 
 * 
 *
 * @author rodche
 */
@Configurable
public abstract class AbstractRule<T> implements Rule<T> {

    protected Log logger = LogFactory.getLog(AbstractRule.class);
    protected Behavior behavior;
    private String tip;
	private boolean postModelOnly = true;
    
    @Resource
    private MessageSource rulesMessageSource;
    
    @Autowired
    private Messenger messenger;
    
    public AbstractRule() {
        logger = LogFactory.getLog(this.getClass()); // concrete class
    }
        

    /**
     * TODO override, if required, in subclasses
     */
    public void fix(T t, Object... values) {
    }
           
    /**
     * Rule setup.
     * 
     * This @PostConstruct initialization method, which
     * can be also called directly, makes to 
     * assign Behavior and Tip properties values using 
     * those found in the properties file, or defaults,
     * if none found.
     * 
     */
	@PostConstruct
	public void init() {
		/*
		 * set value as specified in the properties file, or use the default
		 * value (it doesn't throw exceptions or return null)
		 */
		if (rulesMessageSource != null) {
			// behavior
			String value = rulesMessageSource.getMessage(getName()
					+ ".behavior", null, "ERROR", Locale.getDefault());
			setBehavior(Behavior.valueOf(value.toUpperCase()));

			// description
			this.tip = rulesMessageSource.getMessage(getName(), null, "",
					Locale.getDefault());
			if (tip == null || "".equals(tip)) {
				tip = "description is not found in the messages.properties file";
			} else {
				tip = StringEscapeUtils.escapeHtml(tip);
			}

			// set isPostModelOnly
			String pmo = rulesMessageSource.getMessage(getName()
					+ ".postmodelonly", null, "true", Locale.getDefault());
			setPostModelOnly(Boolean.parseBoolean(pmo));
		} else {
			if(logger.isInfoEnabled()) {
				logger.info("using no configuration" +
						"(rule created outside the Validator context?)" +
						"; messageSource=null.");
			}
		}
	}

    /**
     * Rule's (Spring bean) name.
     * 
     * This is also used for the error reporting.
     * 
     * @return bean name
     */
	public final String getName() {
        if (getClass().getCanonicalName() != null) {
        	String s = getClass().getSimpleName();
			return s.substring(0, 1).toLowerCase() + s.substring(1);
		} else {
			String s = getClass().getName();
			int i = s.lastIndexOf('.');
			return s.substring(i+1);
		}
    }

	/**
	 * Rule description (or 'tip')
	 * 
	 * @return a tip
	 */
    public final String getTip() {
		return tip;
	}
 
    public final Behavior getBehavior() {
        return behavior;
    }

    public final void setBehavior(Behavior behavior) {
        this.behavior = behavior;
    }
             

	/**
     * Call this method from a validation rule implementation 
     * every time when a new error case is found! 
     * 
     * Although not required when using AspectJ LTW only, 
     * this, however, allows for Spring's proxy-based AOP aspects 
     * (one may want to use when integrating the BioPAX validation framework with other applications) 
     * 
     * This particularly helps to resolve one of the problems discussed here: 
     * http://trulsjor.wordpress.com/2009/08/10/spring-aop-the-silver-bullet/
     * (previously, Rule.check method called Rule.error method...)
     * 
     * @param object that is invalid or caused the error
     * @param code error code, e.g., 'illegal.value'
     * @param args extra parameters for the error message template
     */
    protected void error(Object object, String code, Object... args) {
    	Messenger m = getMessenger();
    	if(m != null) {
    		m.sendErrorCase(this, object, code, args); // to be processed...
    	} else {
    		logger.error(this.getName() + 
    			": cannot register the validation error due to the messenger object is null");
    		throw new BiopaxValidatorException(code, 
    				BiopaxValidatorUtils.getId(object), args);
    	}
    }
       
    public boolean isPostModelOnly() {
    	return postModelOnly;
    }
    
    public void setPostModelOnly(boolean postModelOnly) {
		this.postModelOnly = postModelOnly;
	}
    
    
    public MessageSource getRulesMessageSource() {
		return rulesMessageSource;
	}
    
    public void setRulesMessageSource(MessageSource rulesMessageSource) {
		this.rulesMessageSource = rulesMessageSource;
	}
    
    public Messenger getMessenger() {
		return messenger;
	}
    
    public void setMessenger(Messenger messenger) {
		this.messenger = messenger;
	}
}
