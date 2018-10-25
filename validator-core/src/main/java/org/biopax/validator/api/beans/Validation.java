package org.biopax.validator.api.beans;


import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.bind.annotation.*;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.validator.api.Identifier;
import org.biopax.validator.api.Rule;


@XmlType(name="Validation")
@XmlAccessorType(XmlAccessType.FIELD)
public class Validation implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final Log log = LogFactory.getLog(Validation.class);
	
	@XmlTransient
	private Object model;
	// "forcedly" associated objects, i.e., parser, model(s), and dangling elements 
	@XmlTransient
	private final Set<Object> objects;
	// extra/optional settings
	@XmlTransient
	private final Properties properties;
	// getting object's ID strategy (for error reporting)
	@XmlTransient
	private final Identifier idCalc;
	
	
	@XmlElement(required=false)
	private String modelData; //cannot store more than ~1Gb data.
	@XmlElement
	private final Set<ErrorType> error;
	@XmlAttribute(required=false)
	private String description;
	@XmlElement
	private final Set<String> comment;
	@XmlAttribute
	private int notFixedProblems = 0;
	@XmlAttribute
	private int notFixedErrors = 0;
	@XmlAttribute
	private int totalProblemsFound = 0;
	@XmlAttribute
	private boolean fix = false;
	@XmlAttribute(required=false)
	private Behavior threshold;	
	// limit not fixed error cases (1 means "fall-fast" mode, i.e., stop after the first serious and not fixed error)
	@XmlAttribute(required=false)
	private int maxErrors;
	@XmlAttribute(required=false)
	private String profile;
	@XmlAttribute
	private String summary;
	
	
	/** 
	 * Default Constructor (this is mainly for OXM)
	 * @param idCalculator a strategy object to get a domain-specific identifier (for reporting)
	 */
	public Validation(Identifier idCalculator) {
		this.error = new TreeSet<ErrorType>();
		this.objects = Collections.newSetFromMap(new ConcurrentHashMap<Object, Boolean>());
		this.description = "unknown";
		this.comment = new HashSet<String>();
		this.fix = false;
		this.threshold = Behavior.WARNING;
		this.maxErrors = Integer.MAX_VALUE;
		this.profile = null;
		this.properties = new Properties();
		this.idCalc = (idCalculator != null) ? idCalculator : new Identifier() {			
			// fall-back Identifier strategy implementation that uses toString
			public String identify(Object obj) {
				return String.valueOf(obj);
			}
		} ;
	}

	
	/** 
	 * Default Constructor (this is mainly for OXM)
	 */
	public Validation() {
		this(null);
	}
	

	/**
	 * Non-default settings Constructor. 
	 * Default values will be used if null or zero values were provided.
	 * @param idCalculator a strategy object to get a domain-specific identifier (for reporting)
	 * @param description default is "unknown"
	 * @param autoFix default is false
	 * @param errorLevel default is WARNING
	 * @param maxErrors default is 0 (unlimited, actually it's {@link Integer#MAX_VALUE}, but the effect is same)
	 * @param profile validation profile name (if null, the default is used)
	 * 
	 * @throws IllegalArgumentException when maxErrors value is negative
	 */
	public Validation(Identifier idCalculator, String description, boolean autoFix,
										Behavior errorLevel, int maxErrors, String profile) {
		this(idCalculator);
		
		setDescription(description);
		
		this.fix = autoFix;
		
		if(errorLevel != null)
			this.threshold = errorLevel;
		
		if(maxErrors < 0)
			throw new IllegalArgumentException("Illegal value for maxErrors: " + maxErrors);
		this.maxErrors = maxErrors;
		
		if(profile != null && !profile.isEmpty())
			this.profile = profile;
	}

	
	/**
	 * Gets the current Model.
	 * 
	 * @return model
	 */
	@XmlTransient
	public Object getModel() {
		return model;
	}

	
	/**
	 * Sets Model (to check or report about)
	 * 
	 * Note: use with care, because 
	 * 'objects' may still contain elements from 
	 * the old model, which is up to developer to clean them
	 * or continue working with; different models can share the same 
	 * objects, etc.
	 * 
	 * @param model model
	 */
	public void setModel(Object model) {
		this.model = model;
	}


	/**
	 * List of error types; each item has unique code.
	 * 
	 * (Note: the property name is 'Error', and not 'Errors',
	 * simply for the simpler object-XML binding.)
	 * 
	 * @return errors (each has unique error code, e.g., syntax.error)
	 */
	public Collection<ErrorType> getError() {
		return error;
	}


	/**
	 * This method should never be used (it's only used by the Spring OXM framework)
	 * 
	 * @param errors collected errors
	 */
	public synchronized void setError(Collection<ErrorType> errors) {
		error.clear();
		error.addAll(errors);
	}


	/**
	 * This method should never be used
	 * outside this framework;
	 * this is for the biopax-validator Web
	 * server and client applications only.
	 * 
	 * @return BioPAX RDF/XML
	 */
	public String getModelData() 
	{
		return modelData;
	}
	
	/**
	 * This method should never be used
	 * outside this framework;
	 * this is for the biopax-validator Web 
	 * server and client applications only.
	 *
	 * @param modelData serialized model/data
	 */
	public void setModelData(String modelData) {
		this.modelData = modelData;
	}

	/**
	 * Returns the data as HTML-escaped string 
	 * (to show on a web page).
	 * 
	 * @return serialized/encoded object model ok to embed into an html code
	 */
	@XmlTransient
	public String getModelDataHtmlEscaped() {
		return (modelData != null)
				? StringEscapeUtils.escapeHtml4(modelData)
					.replaceAll(System.getProperty("line.separator"), 
						System.getProperty("line.separator")+"<br/>")
				: null;
	}


	/**
	 * Adds or updates the error (with cases) to the errors collection.
	 * This is the primary method to save or update a just discovered or 
	 * fixed validation problem!
	 * 
	 * If the same 'error' already exists there 
	 * (i.e. type:code where type is either 'warning' or 'error'), 
	 * the new error cases will be copied to it;
	 * otherwise, the new one is simply added to the set.
	 * 
	 * It also updates the not-fixed errors counter,
	 * taking into account current validation threshold (level)
	 * and error's own type {@link Behavior} set by a validation rule
	 * that created it!
	 * 
	 * @see #setThreshold(Behavior)
	 * @see ErrorType#hashCode()
	 * @see ErrorType#equals(Object)
	 * 
	 * @param e Error type
	 */
	public synchronized void addError(ErrorType e) {	
		
		int numNotFixedErrors = countErrors(null, null, null, null, true, true);

		if(isMaxErrorsSet() && numNotFixedErrors >= getMaxErrors())
		{
			log.info("Won't save the case: max. errors " +
				"limit exceeded for " + getDescription());
			return;
		}	
		
		switch (threshold) {
		case IGNORE:
			break; // do nothing
		case ERROR:
			if(e.getType() == Behavior.WARNING) {
				break; // do not add (only errors pass)
			}
		default: // add error with all cases
			synchronized (this) {
				if (error.contains(e)) {
					for (ErrorType et : error) {
						if (et.equals(e)) {
							et.addCases(e.getErrorCase());
							break;
						}
					}
				} else { // adding a new error type (code)
					error.add(e);
				}
			}
			break;
		}
	}
	

	/**
	 * Removes the error type and all cases.
	 * @param e error
	 */
	public synchronized void removeError(ErrorType e) {
		error.remove(e);
	}
		

	/**
	 * Sets the information about this validation task. 
	 * 
	 * @param description of the object model (to be validated)
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	

	/**
	 * Information about this validation task. 
	 * 
	 * @return description of the object model (to be validated)
	 */
	public String getDescription() {
		return description;
	}

	
	/**
	 * This method should never be used
	 * (this is for OXM frameworks only)
	 *
	 * @param comments some info
	 */
	public void setComment(Collection<String> comments) {
		this.comment.clear();
		this.comment.addAll(comments);
	}
	

	/**
	 * Add some details, stats regarding the validation task.
	 * @param comment some info
	 */
	public void addComment(String comment) {
		this.comment.add(comment);
	}
	

	/**
	 * Comments (e.g., counts) about this validation task.
	 * @return strings
	 */
	public Collection<String> getComment() {
		return comment;
	}
	
	
	/**
	 * Validation results summary.
	 * @return summary
	 */
	public synchronized String getSummary() {
		return summary;
	}
	public void setSummary(String summary) {
		this.summary = summary;
	}
	
	
	/**
	 * String representation of this validation settings and results:
	 * {@link #getDescription()} plus {@link #getSummary()}
	 * @return some info, such as description and summary
	 */
	@Override
	public String toString() {
		return super.toString() + 
		" (" + getDescription() + "; "
		+ getSummary() + ")";
	}

	
	/**
	 * Searches for existing (registered) error type.
	 * This is mostly for testing.
	 * 
	 * @param code error code
	 * @param type level
	 * @return matching error class
	 */
	public ErrorType findErrorType(String code, Behavior type) {
		return findErrorType(new ErrorType(code, type));
	}
	
	/*
	 * Finds the same error type among
	 * existing (already happened to have error cases)
	 * 
	 * @see ErrorType#equals(Object)
	 * @see ErrorType#hashCode()
	 *
	 */
	private ErrorType findErrorType(ErrorType errorType) {
		if(getError().contains(errorType)) {
			for(ErrorType et: getError()) {
				if(et.equals(errorType)) {
					return et;
				}
			}
		} 
		return null;
	}
	
	
	/*
	 * Finds the same error case in the registry.
	 * 
	 * @see ErrorType#equals(Object)
	 * @see ErrorType#hashCode()
	 * @see ErrorCaseType#equals(Object)
	 * @see ErrorCaseType#hashCode()
	 *
	 */
	private ErrorCaseType findErrorCase(ErrorType errorType, ErrorCaseType errCase) {
		ErrorType etype = findErrorType(errorType);
		if (etype != null) {
			ErrorCaseType ecase = etype.findErrorCase(errCase);
			return ecase; //bug fixed ('return' was missing)
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
	 * @return number of error cases
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


	/**
	 * Objects associated with this validation by the Validator,
	 * usually - indirectly, during I/O operations. 
	 * Any object O can be potentially linked to the Validation object 
	 * if it makes sense, and if there is a {@link Rule} or some method
	 * (such as in AOP Aspects) which is about to register errors about O.
	 * 
	 * @return objects
	 */
	@XmlTransient
	public Set<Object> getObjects() {
		return objects;
	}


	/**
	 * @return true if auto-fix is enabled; otherwise - false
	 */
	public boolean isFix() {
		return fix;
	}


	/**
	 * @param fix true when auto-fix is enabled
	 */
	protected void setFix(boolean fix) {
		this.fix = fix;
	}


	/**
	 * Gets the error reporting level/threshold, i.e., 
	 * this is to ignore issues reported with a level
	 * less than desired; e.g., if 'ERROR' then hide all warnings (similar to logging).
	 * 
	 * @return min. error level
	 */
	public Behavior getThreshold() {
		return threshold;
	}

	/**
	 * Sets the min. error reporting level.
	 * 
	 * @see #getThreshold()
	 * 
	 * @param threshold minimal level (of ignore, warning, error)
	 */
	protected void setThreshold(Behavior threshold) {
		this.threshold = threshold;
	}
	
	/**
	 * Total number of {@link Behavior#ERROR} and 
	 * {@link Behavior#WARNING} cases (either fixed or not).
	 * 
	 * @return number of different type of error/warning
	 */
	public int getTotalProblemsFound() {
		return totalProblemsFound;
	}
	public void setTotalProblemsFound(int n) {
		this.totalProblemsFound = n;
	}


	/**
	 * Total number of {@link Behavior#ERROR} and 
	 * {@link Behavior#WARNING} cases, NOT fixed.
	 * @return the no. not fixed warning or error cases
	 */
	public synchronized int getNotFixedProblems() {
		return notFixedProblems;
	}
	public void setNotFixedProblems(int n) {
		notFixedProblems = n;
	}

	/** 
	 * Total number of NOT fixed {@link Behavior#ERROR} cases.
	 * @return the no. not fixed error cases
	 */
	public synchronized int getNotFixedErrors() {
		return notFixedErrors;
	}
	public void setNotFixedErrors(int n) {
		notFixedErrors = n;
	}


	/** 
	 * Finds a previously reported
	 * by the rule error case and marks it as fixed.
	 * 
	 * @param objectId model element identifier (associated with the error)
	 * @param rule a validation rule name (that reports the error code)
	 * @param errCode specific error code
	 * @param newMsg a message, if not null/empty, to replace the original one
	 */	
	public void setFixed(String objectId, String rule, String errCode, String newMsg) 
	{
		Behavior type = Behavior.WARNING;
		ErrorCaseType ect = findErrorCase(new ErrorType(errCode, type), 
			new ErrorCaseType(rule, objectId, null)); // msg is ignored when comparing errors anyway
		
		if(ect == null) {
			type = Behavior.ERROR;
			ect = findErrorCase(
				new ErrorType(errCode, type), 
				new ErrorCaseType(rule, objectId, null));
		}
		
		if(ect != null && !ect.isFixed()) {
			ect.setFixed(true);
			if(newMsg != null && !"".equals(newMsg.trim()))
				ect.setMessage(newMsg);
		}
	}

	
	/**
	 * Errors limit. After this value is reached, the validator stops registering new error cases
	 * with this validation object (though rules continue to check and report). 
	 * If this method returns 0 that actually means "not set", which also means 'unlimited'
	 * (this is to avoid generating unnecessary XML attribute value, such as {@link Integer#MAX_VALUE}).
	 * 
	 * @return the max no. errors to be collected, or 0 if {@link #isMaxErrorsSet()} returns 'false'
	 */
	public int getMaxErrors() {
		return (isMaxErrorsSet()) ? maxErrors : 0;
	}

	
	/**
	 * Sets the errors threshold (max no. errors to collect/report),
	 * an integer value between 0 and {@link Integer#MAX_VALUE}, otherwise
	 * it has no effect ({@link #isMaxErrorsSet()} will return 'false').
	 * 
	 * @param maxErrors max no. errors to collect
	 */
	protected void setMaxErrors(int maxErrors) {
		this.maxErrors = maxErrors;
	}
	

	/**
	 * @return true iif {@link #maxErrors} is greater than 0 and less than {@link Integer#MAX_VALUE}
	 */
	@XmlTransient
	public boolean isMaxErrorsSet() {
		return this.maxErrors > 0 
			&& this.maxErrors < Integer.MAX_VALUE;
	}

	
	/**
	 * Gets normalizer settings.
	 * @return settings
	 */
	@XmlTransient
	public Properties getProperties() {
		return properties;
	}


	/**
	 * Validation profile (a set of active rules and levels)
	 * to apply when checking/reporting issues.
	 * 
	 * @return the validation profile
	 */
	public String getProfile() {
		return profile;
	}


	/**
	 * Sets the validation profile to use when checking/reporting issues.
	 * 
	 * @param profile the validation profile name
	 */
	protected void setProfile(String profile) {
		this.profile = profile;
	}
	
	
	/**
	 * Gets a domain-specific identifier 
	 * for a model element, stream, parser, etc.
	 * objects related to this validation.
	 * (for error reporting)
	 * 
	 * @param obj model object
	 * @return id
	 */
	public String identify(Object obj) {
		return idCalc.identify(obj);
	}

} 