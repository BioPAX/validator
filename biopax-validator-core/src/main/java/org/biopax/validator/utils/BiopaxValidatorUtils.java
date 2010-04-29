package org.biopax.validator.utils;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.Named;
import org.biopax.validator.Behavior;
import org.biopax.validator.result.*;

import java.io.IOException;
import java.io.Writer;
import java.util.*;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.MessageSource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.oxm.Marshaller;
import org.springframework.util.ResourceUtils;

import org.w3c.dom.*;

/**
 * A utility class.
 * 
 * This is injected into other beans, keeps several global settings and objects,
 * e.g., marshaller, and also provides static service methods to register, 
 * merge, do OXM, and resolve validation errors to human-readable verbose messages.  
 *
 * @author rodche
 */
@Configurable
public class BiopaxValidatorUtils {
    private static final Log logger  = LogFactory.getLog(BiopaxValidatorUtils.class);
    
    private Locale locale;
    private MessageSource messageSource; 
    private static Marshaller resultsMarshaller;
    private final Set<String> ignoredCodes;
    public static int maxErrors = Integer.MAX_VALUE;
    
   
    public BiopaxValidatorUtils() {
		
		this.ignoredCodes = new HashSet<String>();
		this.locale = Locale.getDefault();
	}
    
    public void setLocale(Locale locale) {
		this.locale = locale;
	}
    
    public Locale getLocale() {
		return locale;
	}
    
    public void setMarshaller(Marshaller marshaller) {
		resultsMarshaller = marshaller;
	}
    
	/**
	 * Gets the OXM resultsMarshaller (for validation results.)
	 * @return
	 */
	public Marshaller getMarshaller() {
		return resultsMarshaller;
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
     * TODO implement this feature in the next release (currently it's ignored).
     */
    public void setMaxErrors(int max) {
        maxErrors = max;
    }
    
	/**
	 * Gets object's "id" to use in error messages.
	 * 
	 * If Object is BioPAXElement - its RDFid suffix (after #),
     * otherwise - simple class name + hash code.
     * 
	 * @param obj
	 * @return string id
	 */
    public static String getId(Object obj) {
    	String id = "";
		if (obj instanceof BioPAXElement 
				&& ((BioPAXElement)obj).getRDFId() != null) {
			id = getLocalId((BioPAXElement)obj);
		} else {
			id = "" + obj;
		}
		return id;
	}
    

    /**
     * Gets the local part of the BioPAX element ID.
     * 
     * @param bpe
     * @return
     */
   	public static String getLocalId(BioPAXElement bpe) {
		String id = bpe.getRDFId();
		return (id != null) ? id.replaceFirst("^.+#", "") : null;
	}
    
    /**
     * This is mainly to remove the curly braces 
     * that may cause an exception during 
     * MessageSource resolves the arguments.
     * 
     * @param args
     * @return
     */
    public static String[] fixMessageArgs(Object... args) {
    	String[] newArgs = new String[args.length];
    	int i=0;
    	for(Object a: args) {
    		if(a != null) {
    			String s = (a instanceof String) 
        			? (String)a : getId(a);
    			if (s.contains("{") || s.contains("}")) {
    				s.replaceAll("\\}", ")");
    				newArgs[i] = s.replaceAll("\\{", "(");
    			} else {
    				newArgs[i] = s;
    			}
    		} else {
    			newArgs[i] = "N/A";
    		}
    		i++;
    	}
    	return newArgs;
	}
            
    public static String toString(Object... args) {
    	StringBuffer sb = new StringBuffer();
		String params[] = BiopaxValidatorUtils.fixMessageArgs(args);
		for (String p : params) {
			sb.append("\"").append(p).append("\"; ");
		}
		return sb.toString();
    }
    
    public static String getIdListAsString(Collection<?> objects) {
    	StringBuffer sb = new StringBuffer();
		for (Object o : objects) {
			sb.append("\"").append(getId(o)).append("\"; ");
		}
		return sb.toString();
    }
    
    
    /**
     * Adds codes for the corresponding errors to be ignored
     * (not shown in the validation report).
     * 
     * @param codes Set of error codes.
     */
    public void addIgnoredCodes(Set<String> codes) {
		if(logger.isTraceEnabled()) {
			logger.trace("adding ignored codes");
		}
		
		this.ignoredCodes.addAll(codes);
		
		if(logger.isTraceEnabled()) {
			StringBuffer sb = new StringBuffer();
			for(String c: ignoredCodes) {
				sb.append(c + " ");
			}
			logger.trace("(after adding) codes to ignore: " + sb.toString());
		}
	}
    
    /**
     * Removes codes from the ignored set.
     * 
     * @param codes codes to free
     */
    public void removeIgnoredCodes(Set<String> codes) {
		if(logger.isTraceEnabled()) {
			logger.trace("removing ignored codes");
		}
		
		this.ignoredCodes.removeAll(codes);
		
		if(logger.isTraceEnabled()) {
			StringBuffer sb = new StringBuffer();
			for(String c: ignoredCodes) {
				sb.append(c + " ");
			}
			logger.trace("(after removal) codes to ignore: " + sb.toString());
		}
	}
    
    public Set<String> getIgnoredCodes() {
		return ignoredCodes;
	}
    
    /**
     * Tests whether the error code should be ignored.
     * 
     * @param errorCode
     * @return
     */
    public boolean isIgnoredCode(String errorCode) {
    	return getIgnoredCodes().contains(errorCode)
    	|| getIgnoredCodes().contains("all");
    }
 	
	/**
 	 * Writes the multiple results report.
	 * (as transformed XML).
	 * 
	 * @param validatorResponse
	 * @param writer
	 */
	public static void write(ValidatorResponse validatorResponse, Writer writer, Source xslt) {
		Document doc = asDocument(validatorResponse);
		transformAndWrite(doc, writer, xslt);
	}
	
	/**
 	 * Writes the single validation report.
	 * (as transformed XML).
	 * 
	 * @param validationResult
	 * @param writer
	 */
	public void write(Validation validationResult, Writer writer, Source xslt) {
		Element el = asElement(validationResult);
		transformAndWrite(el, writer, xslt);
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
	public Element asElement(Validation validationResult) {
		DOMResult domResult = marshal(validationResult);
		Element validation = (Element) domResult.getNode().getFirstChild();
		return validation;
	}
	
	
	public static void transformAndWrite(Node resultNode, Writer writer, Source xsltSource) {
		try {
				Source xmlSource = new DOMSource(resultNode);
				Result result = new StreamResult(writer);
				TransformerFactory transFact = TransformerFactory.newInstance();
				Transformer trans = null; 
				if (xsltSource != null) {
					trans = transFact.newTransformer(xsltSource);
				} else {
					trans = transFact.newTransformer();
				}
				trans.transform(xmlSource, result);
		} catch (Exception e) {
			throw new RuntimeException("Cannot transform/write.", e);
		} 
	}
	
	
	protected static DOMResult marshal(Object obj) {
		DOMResult domResult = new DOMResult();
		try {
			resultsMarshaller.marshal(obj, domResult);
		} catch (Exception e) {
			throw new RuntimeException("Cannot serialize object: " + obj.getClass().getSimpleName(), e);
		} 
		if(logger.isDebugEnabled()) {
			logger.debug(obj.getClass().getSimpleName()+ " is serialized: " + domResult.getNode().getNodeName());
		}
		return domResult;
	}

    
    public ErrorType createError(String objectName, String errorCode, 
    		String ruleName, Behavior warnOrErr, Object... msgArgs) {
		
    	if(objectName == null) {
    		objectName = "null";
    		logger.warn("Creating an error " + errorCode + " for Null object!");
    	}
    	
    	ErrorType error = new ErrorType(errorCode, warnOrErr);
		
		String commonMsg = messageSource.getMessage(errorCode + ".default",
				new Object[]{}, "No description.", locale);
		error.setMessage(commonMsg);
		
		String[] args = BiopaxValidatorUtils.fixMessageArgs(msgArgs);
		
		String msg = messageSource.getMessage(
				errorCode, args, toString(msgArgs), locale).replaceAll("\r|\n+", " ");
		error.addErrorCase(new ErrorCaseType(ruleName, objectName, msg));
		
		return error;
    }
    
    
    /**
     * Gets (named) two entities' common names.
     * 
     * @param e1 
     * @param e2
     * @return
     */
    public static Collection<?> namesInCommon(final Named e1, final Named e2) {
		Set<Object> names = new HashSet<Object>(e1.getName().size()); 
    	names.addAll(e1.getName());
    	names.retainAll(e2.getName());
    	return names;
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

}
