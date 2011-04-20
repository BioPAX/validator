package org.biopax.validator.result;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

import javax.xml.bind.annotation.*;

import org.apache.commons.lang.StringEscapeUtils;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.util.AbstractFilterSet;
import org.biopax.validator.utils.BiopaxValidatorException;
import org.biopax.validator.utils.Normalizer.NormalizerOptions;


@XmlType(name="Validation", namespace="http://biopax.org/validator/2.0/schema")
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class Validation implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private Model model;
	private String modelSerialized;
	private final Set<ErrorType> error;
	private String description;
	private final Set<String> comment;
	
	// "forcedly" associated objects, i.e., parser, model(s), and dangling elements 
	private final Set<Object> objects;
	
	private boolean fix = false;
	private Behavior threshold;
	private boolean normalize = false;
	
	private int maxErrors; // limits the num. of not fixed error cases (1 means "fal-fast" mode, i.e., stop after the first serious and not fixed error)

	// extra options for the normalizer (optional)
	private NormalizerOptions normalizerOptions;
	
	// Default Constructor (this is mainly for OXM)
	public Validation() {
		this.error = new TreeSet<ErrorType>();
		this.description = "unknown";
		this.comment = new HashSet<String>();
		this.objects = new HashSet<Object>();
		this.fix = false;
		this.normalize = false;
		this.threshold = Behavior.WARNING;
		this.maxErrors = Integer.MAX_VALUE;
	}

	// Constructor that is used in the Validator
	public Validation(String name) {
		this();
		description = name;
	}	
	
	/**
	 * Gets the current Model.
	 * 
	 * @return
	 */
	@XmlTransient
	public Model getModel() {
		return model;
	}
	
	/**
	 * Set Model (to check or report about)
	 * 
	 * Note: unsafe, so use with care! 
	 * ('objects' may still contain elements from 
	 * the old model, which is up to developer to clean them
	 * or continue working with; different models can share the same 
	 * objects, etc...)
	 * 
	 * @param model
	 */
	public void setModel(Model model) {
		this.model = model;
	}

	
	/**
	 * List of error types; each item has unique code.
	 * 
	 * (Note: the property name is 'Error', and not 'Errors',
	 * simply for the simpler object-XML binding.)
	 * 
	 * @return
	 */
	public Collection<ErrorType> getError() {
		return error;
	}

	public void setError(Collection<ErrorType> errors) {
		error.clear();
		error.addAll(errors);
	}

	
	/**
	 * This is to refresh 'modelSerialized' property: 
	 * 	generates BioPAX OWL from current model.
	 */
	public void updateModelSerialized() {
		Model model = getModel();
		if (model != null) {
			// update the OWL data
			try {
				// export to OWL
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				(new SimpleIOHandler(model.getLevel())).convertToOWL(model, outputStream);
				this.modelSerialized = outputStream.toString("UTF-8");
			} catch (IOException e) {
				throw new BiopaxValidatorException(
						"Failed to export modified model!", e);
			}
		}
	}
	
	
	/**
	 * Returns current BioPAX OWL
	 * 
	 * @return
	 */
	@XmlElement(required = false)
	public String getModelSerialized() 
	{
		return modelSerialized;
	}
	
	public void setModelSerialized(String modelSerialized) {
		this.modelSerialized = modelSerialized;
	}

	/**
	 * Returns current BioPAX OWL in 
	 * the HTML-escaped form (to show on pages).
	 * 
	 * @return
	 */
	@XmlTransient
	public String getModelSerializedHtmlEscaped() {
		return StringEscapeUtils.escapeHtml(getModelSerialized())
		.replaceAll(System.getProperty("line.separator"), 
				System.getProperty("line.separator")+"<br/>");
	}

	/**
	 * Returns current BioPAX OWL in 
	 * the XML-escaped form 
	 * (to include inside another XML data).
	 * 
	 * @return
	 */
	@XmlTransient
	public String getModelSerializedXmlEscaped() {
		return StringEscapeUtils.escapeXml(getModelSerialized());
	}

	
	/**
	 * Adds the error (with cases) to the collection.
	 * 
	 * If the same 'error' already exists there 
	 * (i.e. type:code where type is either 'warning' or 'error'), 
	 * the new error cases will be copied to it;
	 * otherwise, the new one is simply added to the set.
	 * 
	 * It also takes the 'error threshold' (level) into account!
	 * 
	 * @see #setThreshold(Behavior)
	 * @see ErrorType#hashCode()
	 * @see ErrorType#equals(Object)
	 * 
	 * @param e Error type
	 */
	public void addError(ErrorType e) {	
		switch (threshold) {
		case IGNORE:
			break; // do nothing
		case ERROR:
			if(e.getType() == Behavior.WARNING) {
				break; // do not add (only errors pass)
			}
		default: // WARNING and ERROR==ERROR
			// add error with all cases
			if (error.contains(e)) {
				for (ErrorType et : error) {
					if (et.equals(e)) {
						et.addCases(e.getErrorCase());
						return;
					}
				}
			} else {
				error.add(e);
			}
			break;
		}
	}
	
	public void removeError(ErrorType e) {
		error.remove(e);
	}
		
	public void setDescription(String description) {
		this.description = description;
	}
	
	@XmlAttribute(required=false)
	public String getDescription() {
		return description;
	}
	
	public void setComment(Collection<String> comments) {
		this.comment.clear();
		this.comment.addAll(comments);
	}
	
	public void addComment(String comment) {
		this.comment.add(comment);
	}
	
	public Collection<String> getComment() {
		return comment;
	}
	
	
	@XmlAttribute
	public String getSummary() {
		StringBuffer result = new StringBuffer();
		if (error.size()>0) { 
			result.append("different types of problem: ");
			result.append(error.size());
		}
		return result.toString();
	}
	
	@Override
	public String toString() {
		return super.toString() + 
		" (" + getDescription() + "; "
		+ getSummary() + ")";
	}
	
	/**
	 * Searches for existing (registered) error type.
	 * 
	 * @param code
	 * @param type
	 * @return
	 */
	public ErrorType findErrorType(String code, Behavior type) {
		return findErrorType(new ErrorType(code, type));
	}
	
	/**
	 * Finds the same error type among
	 * existing (already happened to have error cases)
	 * 
	 * @see ErrorType#equals(Object)
	 * @see ErrorType#hashCode()
	 * 
	 * @param errorType
	 * @return
	 */
	public ErrorType findErrorType(ErrorType errorType) {
		if(getError().contains(errorType)) {
			for(ErrorType et: getError()) {
				if(et.equals(errorType)) {
					return et;
				}
			}
		} 
		return null;
	}
	
	
	/**
	 * Finds the same error case in the registry.
	 * 
	 * @see ErrorType#equals(Object)
	 * @see ErrorType#hashCode()
	 * @see ErrorCaseType#equals(Object)
	 * @see ErrorCaseType#hashCode()
	 *  
	 * @param errorType
	 * @return
	 */
	public ErrorCaseType findErrorCase(ErrorType errorType, ErrorCaseType errCase) {
		ErrorType etype = findErrorType(errorType);
		if (etype != null) {
			ErrorCaseType ecase = etype.findErrorCase(errCase);
		}
		return null;
	}
	
	/**
	 * Counts the number of errors/warnings.
	 * Extra parameters are used to exclude 
	 * some of the cases.
	 * 
	 * @param forObject when 'null', counts all
	 * @param reportedBy when 'null', counts all
	 * @param code when 'null', counts all
	 * @param category when 'null', counts all
	 * @param ignoreWarnings do not count WARNINGs
	 * @param ignoreFixed do not count fixed
	 * @return
	 */
	public int countErrors(String forObject, String reportedBy, 
			String code, Category category, boolean ignoreWarnings, boolean ignoreFixed) {
		int count = 0;
		
		for(ErrorType et : getError()) {
			// skip warnings?
			if(ignoreWarnings == true && et.getType() == Behavior.WARNING) {
				continue;
			}
			
			// skip other codes?
			if(code != null && !code.equalsIgnoreCase(et.getCode())) {
				continue;
			}
			
			// skip other categories?
			if(category != null && !(category == et.getCategory())) {
				continue;
			}
			
			count += et.countErrors(forObject, reportedBy, ignoreFixed);
		}
		
		return count;
	}

	
	@XmlTransient
	public Set<Object> getObjects() {
		return objects;
	}
	

	public <T> Collection<T> getObjects(final Class<T> filterBy) {
		return new AbstractFilterSet<T>(objects) {
			protected boolean filter(Object value) {
				return filterBy.isInstance(value);
			}			
		};
	}

	@XmlAttribute
	public boolean isFix() {
		return fix;
	}

	public void setFix(boolean fix) {
		this.fix = fix;
	}

	@XmlAttribute(required=false)
	public Behavior getThreshold() {
		return threshold;
	}

	public void setThreshold(Behavior threshold) {
		this.threshold = threshold;
	}
	
	@XmlAttribute
	public boolean isNormalize() {
		return normalize;
	}
	
	public void setNormalize(boolean normalize) {
		this.normalize = normalize;
	}
	
	/**
	 * Total error and warning cases (fixed or not)
	 * 
	 * @return
	 */
	@XmlAttribute
	public int getTotalProblemsFound() {
		return countErrors(null, null, null, null, false, false);
	}

	/**
	 * Total error and warning cases, not fixed.
	 * 
	 * @return
	 */
	@XmlAttribute
	public int getNotFixedProblems() {
		return countErrors(null, null, null, null, false, true);
	}
	
	/** 
	 * Total error cases, not fixed.
	 * 
	 * @return
	 */
	@XmlAttribute
	public int getNotFixedErrors() {
		return countErrors(null, null, null, null, true, true);
	}
	
	/** 
	 * Finds the previously reported
	 * by the rule error case and marks it as fixed.
	 * 
	 * (This method can be used from the {@link Normalizer})
	 * 
	 * @param validation
	 * @param objectId BioPAX element identifier (associated with the error)
	 * @param rule a BioPAX Validator rule ID (that reports with the error code)
	 * @param errCode specific error code
	 * @param newMsg a message, if not null/empty, to replace the original one
	 */
	public static void setFixed(Validation validation, 
		String objectId, String rule, String errCode, String newMsg) 
	{
		if(validation != null) {
			Behavior type = Behavior.WARNING;
			ErrorCaseType ect = validation.findErrorCase(
				new ErrorType(errCode, type), 
				new ErrorCaseType(rule, objectId, null)); // msg is ignored when comparing errors
			if(ect == null) {
				type = Behavior.ERROR;
				ect = validation.findErrorCase(
					new ErrorType(errCode, type), 
					new ErrorCaseType(rule, objectId, null));
			}
			if(ect != null) {
				ect.setFixed(true);
				if(newMsg != null && !"".equals(newMsg.trim()))
					ect.setMessage(newMsg);
			}
		}
	}

	
	/**
	 * @see #setFixed(Validation, String, String, String, String)
	 * 
	 * @param objectId
	 * @param rule
	 * @param errCode
	 * @param newMsg
	 */
	public void setFixed(String objectId, String rule, String errCode, String newMsg) 
	{
		setFixed(this, objectId, rule, errCode, newMsg);
	}
		
	@XmlAttribute(required=false)
	public int getMaxErrors() {
		return (isMaxErrorsSet()) ? maxErrors : 0;
	}

	public void setMaxErrors(int maxErrors) {
		this.maxErrors = maxErrors;
	}
	
	@XmlTransient
	public boolean isMaxErrorsSet() {
		return this.maxErrors > 0 
			&& this.maxErrors < Integer.MAX_VALUE;
	}

	@XmlTransient
	public NormalizerOptions getNormalizerOptions() {
		return normalizerOptions;
	}

	/**
	 * @param normalizerOptions the normalizerOptions to set
	 */
	public void setNormalizerOptions(NormalizerOptions normalizerOptions) {
		this.normalizerOptions = normalizerOptions;
	}
} 