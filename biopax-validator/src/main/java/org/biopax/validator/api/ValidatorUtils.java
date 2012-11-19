package org.biopax.validator.api;


import org.biopax.validator.api.beans.Behavior;
import org.biopax.validator.api.beans.Category;
import org.biopax.validator.api.beans.ErrorCaseType;
import org.biopax.validator.api.beans.ErrorType;
import org.biopax.validator.api.beans.Validation;
import org.biopax.validator.api.beans.ValidatorResponse;

import java.io.IOException;
import java.io.Writer;
import java.util.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.context.MessageSource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import org.w3c.dom.*;

/**
 * Validator Utility Class (not BioPAX specific).
 * 
 * This is injected into other beans, keeps several global settings and objects,
 * e.g., marshaller, and also provides static service methods to register, 
 * merge, do OXM, and resolve validation errors to human-readable verbose messages.  
 *
 * @author rodche
 */
@Service
public class ValidatorUtils {
    private static final Log logger  = LogFactory.getLog(ValidatorUtils.class);
    
    private Locale locale;
    private MessageSource messageSource; 
    //private static Jaxb2Marshaller resultsMarshaller;
    private static JAXBContext jaxbContext;
    private int maxErrors = Integer.MAX_VALUE;
    
    static {
    	try {
			jaxbContext = JAXBContext.newInstance(
					ValidatorResponse.class, Validation.class,
					ErrorCaseType.class, ErrorType.class,
					Behavior.class, Category.class);
		} catch (JAXBException e) {
			throw new RuntimeException("Failed to initialize the " +
				"org.biopax.validator.result JAXB context!", e);
		}
    }
    
    public ValidatorUtils() {
		this.locale = Locale.getDefault();
	}
    
    public void setLocale(Locale locale) {
		this.locale = locale;
	}
    
    public Locale getLocale() {
		return locale;
	}
    
	/**
	 * Gets the results Marshaller (for validation results.)
	 * @return
	 */
	public static Marshaller getMarshaller() {
		//return resultsMarshaller;
		try {
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
			return marshaller;
		} catch (JAXBException e) {
			throw new RuntimeException("Failed to create Marshaller", e);
		}
	}
    
	/**
	 * Gets the results Unmarshaller (for validation results.)
	 * @return
	 */
	public static Unmarshaller getUnmarshaller() {
		//return resultsMarshaller;
		try {
			return jaxbContext.createUnmarshaller();
		} catch (JAXBException e) {
			throw new RuntimeException("Failed to create Unmarshaller", e);
		}
	}
	
	
	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	public MessageSource getMessageSource() {
		return messageSource;
	}
	     
	/**
     * Gets current max number of errors to report.
     * 
     * @return
     */
    public int getMaxErrors() {
        return maxErrors;
    }

    /**
     * Sets current max number of errors to report.
     * 
     */
    public void setMaxErrors(int max) {
        maxErrors = max;
    }
   
	
	/**
 	 * Writes the multiple results report.
	 * (as transformed XML).
	 * 
	 * @param validatorResponse
	 * @param writer
	 */
	public static void write(ValidatorResponse validatorResponse,
			Writer writer, Source xslt) 
	{
		try {
			if (xslt != null) {
				Document doc = asDocument(validatorResponse);
				Source xmlSource = new DOMSource(doc);
				Result result = new StreamResult(writer);
				TransformerFactory transFact = TransformerFactory.newInstance();
				Transformer trans = transFact.newTransformer(xslt);
				trans.setOutputProperty(OutputKeys.INDENT, "yes");
				trans.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
				trans.transform(xmlSource, result);
			} else {
				// write without any xslt
				getMarshaller().marshal(validatorResponse, writer);
			}

		} catch (Exception e) {
			throw new RuntimeException("Cannot transform/serialize/write: "
					+ validatorResponse, e);
		}
	}
	
	/**
 	 * Writes the single validation report.
	 * (as transformed XML).
	 * 
	 * @param validationResult
	 * @param writer
	 */
	public static void write(Validation validationResult, Writer writer, Source xslt) {
		ValidatorResponse resp = new ValidatorResponse();
		resp.addValidationResult(validationResult);
		write(resp, writer, xslt);
	}
	
	/**
	 * Converts a validator response (contains one or 
	 * several validation results) into the document.
	 * 
	 * @param validatorResponse
	 * @return
	 */
	public static Document asDocument(ValidatorResponse validatorResponse) {
		DOMResult domResult = marshal(validatorResponse);
		Document validation = (Document) domResult.getNode();
		return validation;
	}
	
	/**
	 * Converts one validation result 
	 * into the DOM element.
	 * 
	 * @param validationResult
	 * @return
	 */
	public static Element asElement(Validation validationResult) {
		DOMResult domResult = marshal(validationResult);
		Element validation = (Element) domResult.getNode().getFirstChild();
		return validation;
	}
	
	
	protected static DOMResult marshal(Object obj) {
		DOMResult domResult = new DOMResult();
		try {
			getMarshaller().marshal(obj, domResult);
		} catch (Exception e) {
			throw new RuntimeException("Cannot serialize object: " + obj, e);
		} 
		return domResult;
	}

	
	/**
	 * Creates an error type with single error case.
	 * 
	 * @param objectName
	 * @param errorCode
	 * @param ruleName
	 * @param profile validation profile
	 * @param isFixed
	 * @param msgArgs
	 * @return
	 */
    public ErrorType createError(String objectName, String errorCode, 
    		String ruleName, String profile, boolean isFixed, 
    		Object... msgArgs) 
    {
    	return createError(messageSource, locale, objectName, errorCode, ruleName, profile, isFixed, msgArgs);
    }
    
	/**
	 * Creates an error type with single error case.
	 * If the message source is null (e.g., in tests, when a rule
	 * is checked outside the validator framework), 
	 * 
	 * @param messageSource
	 * @param locale
	 * @param objectName
	 * @param errorCode
	 * @param ruleName
	 * @param profile validation profile
	 * @param isFixed
	 * @param msgArgs
	 * @return
	 */
    public static ErrorType createError(
    		MessageSource messageSource, Locale locale,
    		String objectName, String errorCode, 
    		String ruleName, String profile, boolean isFixed, 
    		Object... msgArgs) 
    {
		
    	if(objectName == null) {
    		objectName = "null";
    		logger.warn("Creating an error " + errorCode + " for Null object!");
    	}
    	
    	//get/use current behavior
    	Behavior behavior = getRuleBehavior(ruleName, profile, messageSource);
    	// new error object
    	ErrorType error = new ErrorType(errorCode, behavior);
		
    	// build human-friendly messages using default locale and property files (msg sources)
		String commonMsg = (messageSource != null)
			? messageSource.getMessage(errorCode + ".default", new Object[]{}, "No description.", locale)
				: "No description.";
		error.setMessage(commonMsg);
		
		String msg = (messageSource != null)
			? messageSource.getMessage(errorCode, msgArgs, StringUtils.join(msgArgs, "; "), locale).replaceAll("\r|\n+", " ")
				: StringUtils.join(msgArgs, "; ");
		
		// resolve/set BioPAX problem category
		String category = (messageSource != null)
			? messageSource.getMessage(errorCode + ".category", null, Category.INFORMATION.name(), locale)
				: null;
		if(category != null)
			error.setCategory(Category.valueOf(category.trim().toUpperCase()));
		
		// add one error case
		ErrorCaseType errorCase = new ErrorCaseType(ruleName, objectName, msg);
		errorCase.setFixed(isFixed); //
		error.addErrorCase(errorCase);
		
		//done.
		return error;
    }

    
    /**
     * Gets the validator's home directory path.
     * 
     * @return
     * @throws IOException 
     */
    public static String getHomeDir() throws IOException {
		Resource r =  new FileSystemResource(ResourceUtils.getFile("classpath:"));
		return r.createRelative("..").getFile().getCanonicalPath();
    }
    
       
    /**
     * Gets rule's behavior (mode), to be used
     * when registering an error case reported by the rule.
     * 
     * @see Behavior
     * 
     * @param ruleName validation rule class name, e.g., org.biopax.validator.rules.MyRule
     * @param profile validation profile name or null (default profile)
     * @param messageSource 
     * @return
     */
    public Behavior getRuleBehavior(String ruleName, String profile) { 
    	return getRuleBehavior(ruleName, profile, messageSource);
    }
    
    
    /**
     * Gets rule's behavior (mode) during unit testing
     * when messageSource can be null.
     * 
     * @see ValidatorUtils#getRuleBehavior(String, String)
     * @see ValidatorUtils#createError(MessageSource, Locale, String, String, String, String, boolean, Object...)
     * 
     * @param ruleName validation rule class name, e.g., org.biopax.validator.rules.MyRule
     * @param profile validation profile name or null (default profile)
     * @param messageSource 
     * @return
     */
    private static Behavior getRuleBehavior(String ruleName, String profile, MessageSource messageSource) {
    	if(messageSource == null) return Behavior.ERROR;
    	
    	// get the default behavior value first
    	String value = messageSource.getMessage(ruleName + ".behavior", null, "ERROR", Locale.getDefault());
    		
    	// - override from the profile, if set/available
    	if(profile != null && !profile.isEmpty())
    		value = messageSource.getMessage(ruleName + ".behavior." + profile, null, value, Locale.getDefault());
    	
		return Behavior.valueOf(value.toUpperCase());
	}
    
    
    /**
     * 
     * @param ruleName validation rule class name, e.g., org.biopax.validator.rules.MyRule
     * @return
     */
	public String getRuleDescription(String ruleName) {
		
		String tip = messageSource.getMessage(ruleName, null, "",
				Locale.getDefault());
		if (tip == null || "".equals(tip)) {
			tip = "description is not found in the messages.properties file";
		} else {
			tip = StringEscapeUtils.escapeHtml(tip);
		}
		
		return tip;
	}
    
}
