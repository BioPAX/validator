package org.biopax.validator.result;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

import javax.xml.bind.annotation.*;

import org.apache.commons.lang.StringEscapeUtils;
import org.biopax.paxtools.io.simpleIO.SimpleExporter;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.util.AbstractFilterSet;
import org.biopax.validator.Behavior;
import org.biopax.validator.utils.BiopaxValidatorException;


@XmlType//(namespace="http://biopax.org/validator/2.0/schema", name="ValidationResult")
@XmlRootElement(name="validationResult")
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
	
	private Behavior threshold; // TODO implement errors filter!
	
	private boolean normalize = false;
	
	// Default Constructor (this is mainly for OXM)
	public Validation() {
		this.error = new HashSet<ErrorType>();
		this.description = "unknown";
		this.comment = new HashSet<String>();
		this.objects = new HashSet<Object>();
		this.fix = false;
		this.normalize = false;
		this.threshold = Behavior.WARNING;
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
	 * Returns current BioPAX OWL
	 * (if either 'fix' or 'normalize' are true)!
	 * 
	 * @return
	 */
	@XmlElement(required = false)
	public String getModelSerialized() 
	{
		Model model = getModel();
		if(model != null) {
			// update the OWL data
			if (isFix() || isNormalize()) { // TODO may be to remove this condition...
				try {
					// export to OWL
					ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
					(new SimpleExporter(model.getLevel())).convertToOWL(model, outputStream);
					this.modelSerialized = outputStream.toString("UTF-8");
				} catch (IOException e) {
					throw new BiopaxValidatorException(
							"Failed to export modified model!", e);
				}
			}
		}
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
	 * 
	 * @param forObject when 'null', counts all
	 * @param reportedBy when 'null', counts all
	 * @param code when 'null', counts all
	 * @param ignoreWarnings
	 * @return
	 */
	public int countErrors(String forObject, String reportedBy, 
			String code, boolean ignoreWarnings) {
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
			
			count += et.countErrors(forObject, reportedBy);
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
	
	@XmlAttribute
	public int getTotalProblemsFound() {
		return countErrors(null, null, null, false);
	}
} 