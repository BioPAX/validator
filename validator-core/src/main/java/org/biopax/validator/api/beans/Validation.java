package org.biopax.validator.api.beans;

/*
 * #%L
 * Object Model Validator Core
 * %%
 * Copyright (C) 2008 - 2013 University of Toronto (baderlab.org) and Memorial Sloan-Kettering Cancer Center (cbio.mskcc.org)
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.bind.annotation.*;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.validator.api.Identifier;
import org.biopax.validator.api.Rule;


@XmlType(name="Validation")
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class Validation implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final Log log = LogFactory.getLog(Validation.class);
	
	private Object model;
	private String modelData;
	private final Set<ErrorType> error;
	private String description;
	private final Set<String> comment;
	
	// "forcedly" associated objects, i.e., parser, model(s), and dangling elements 
	private final Set<Object> objects;
	
	private boolean fix = false;
	private Behavior threshold;
	
	// limit not fixed error cases (1 means "fall-fast" mode, i.e., stop after the first serious and not fixed error)
	private int maxErrors;
	
	// extra/optional settings
	private final Properties properties;

	private String profile;
	
	/**
	 * Concrete strategy impl. to get an object's ID (for error reporting)
	 */
	private final Identifier idCalc;

	
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
			@Override
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
	 * @param maxErrors default is 0 (means unlimited, actually it's {@link Integer#MAX_VALUE, but the effect is same})
	 * @param profile validation profile name (if null, the default is used)
	 * 
	 * @throws IllegalArgumentException when maxErrors < 0
	 */
	public Validation(Identifier idCalculator, String description, boolean autoFix, Behavior errorLevel, int maxErrors, String profile) {
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
	 * @return
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
	 * @param model
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
	 * @return
	 */
	public synchronized Collection<ErrorType> getError() {
		return error;
	}


	/**
	 * This method should never be used
	 * (this is for OXM frameworks only)!
	 * 
	 * @param errors
	 */
	public synchronized void setError(Collection<ErrorType> errors) {
		error.clear();
		error.addAll(errors);
	}

	
	@XmlElement(required = false)
	public String getModelData() 
	{
		return modelData;
	}

	
	/**
	 * This method should never be used
	 * (this is for OXM frameworks only)!
	 *
	 * @param modelData
	 */
	public void setModelData(String modelData) {
		this.modelData = modelData;
	}


	/**
	 * Returns the data as HTML-escaped string 
	 * (to show on web pages).
	 * 
	 * @return
	 */
	@XmlTransient
	public String getModelDataHtmlEscaped() {
		return (modelData != null)
				? StringEscapeUtils.escapeHtml(getModelData())
					.replaceAll(System.getProperty("line.separator"), 
						System.getProperty("line.separator")+"<br/>")
				: null;
	}


	/**
	 * Returns the data as XML-escaped string 
	 * (to embed inside a XML element).
	 * 
	 * @return
	 */
	@XmlTransient
	public String getModelDataXmlEscaped() {
		return (modelData != null) 
			? StringEscapeUtils.escapeXml(getModelData())
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
	 * It also updates the notfixed errors counter, 
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
	public void addError(ErrorType e) {	
		
		if(isMaxErrorsSet() && getNotFixedErrors() >= getMaxErrors())
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
	 * @param e
	 */
	public synchronized void removeError(ErrorType e) {
		error.remove(e);
	}
		

	/**
	 * Sets the information about this validation task. 
	 * 
	 * @param description
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	

	/**
	 * Information about this validation task. 
	 * 
	 * @return
	 */
	@XmlAttribute(required=false)
	public String getDescription() {
		return description;
	}

	
	/**
	 * This method should never be used
	 * (this is for OXM frameworks only)!
	 *
	 * @param comments
	 */
	public void setComment(Collection<String> comments) {
		this.comment.clear();
		this.comment.addAll(comments);
	}
	

	/**
	 * Adds a comment, e.g., counts, regarding this validation task.
	 * @param comment
	 */
	public void addComment(String comment) {
		this.comment.add(comment);
	}
	

	/**
	 * Comments (e.g., counts) about this validation task.
	 * @return
	 */
	public Collection<String> getComment() {
		return comment;
	}
	
	
	/**
	 * Validation results summary.
	 * 
	 * @return
	 */
	@XmlAttribute
	public synchronized String getSummary() {
		StringBuffer result = new StringBuffer();
		if (error.size()>0) { 
			result.append("different types of problem: ");
			result.append(error.size());
		}
		return result.toString();
	}
	
	
	/**
	 * String representation of this validation settings and results:
	 * {@link #getDescription()} plus {@link #getSummary()}
	 * 
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


	/**
	 * Objects associated with this validation by the Validator,
	 * usually - indirectly, during I/O operations. 
	 * Any object O can be potentially linked to the Validation object 
	 * if it makes sense, and if there is a {@link Rule} or some method
	 * (such as in AOP Aspects) which is about to register errors about O.
	 * 
	 * @return
	 */
	@XmlTransient
	public Set<Object> getObjects() {
		return objects;
	}


	/**
	 * @return true if auto-fix is enabled; otherwise - false
	 */
	@XmlAttribute
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
	 * less than desired; e.g., to hide warnings (similar to logging).
	 * 
	 * @return
	 */
	@XmlAttribute(required=false)
	public Behavior getThreshold() {
		return threshold;
	}


	/**
	 * Sets the errors reporting level.
	 * 
	 * @see #getThreshold()
	 * 
	 * @param threshold
	 */
	protected void setThreshold(Behavior threshold) {
		this.threshold = threshold;
	}
	
	/**
	 * Total number of {@link Behavior#ERROR} and 
	 * {@link Behavior#WARNING} cases (either fixed or not).
	 * 
	 * @return
	 */
	@XmlAttribute
	public int getTotalProblemsFound() {
		return countErrors(null, null, null, null, false, false);
	}


	/**
	 * Total number of {@link Behavior#ERROR} and 
	 * {@link Behavior#WARNING} cases, NOT fixed.
	 * 
	 * @return
	 */
	@XmlAttribute
	public int getNotFixedProblems() {
		return countErrors(null, null, null, null, false, true);
	}
	

	/** 
	 * Total number of NOT fixed {@link Behavior#ERROR} cases.
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
	 * This method is used by {@link Normalizer} to allow
	 * fixing and reporting as 'fixed' for some error cases 
	 * previously found by the validator (using this validation instance).
	 * 
	 * @param objectId model element identifier (associated with the error)
	 * @param rule a validation rule name (that reports the error code)
	 * @param errCode specific error code
	 * @param newMsg a message, if not null/empty, to replace the original one
	 */	
	public void setFixed(String objectId, String rule, String errCode, String newMsg) 
	{
		Behavior type = Behavior.WARNING;
		ErrorCaseType ect = findErrorCase(
			new ErrorType(errCode, type), 
			new ErrorCaseType(rule, objectId, null)); // msg is ignored when comparing errors
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
	@XmlAttribute(required=false)
	public int getMaxErrors() {
		return (isMaxErrorsSet()) ? maxErrors : 0;
	}

	
	/**
	 * Sets the errors threshold (max no. errors to collect/report),
	 * an integer value between 0 and {@link Integer#MAX_VALUE}, otherwise
	 * it has no effect ({@link #isMaxErrorsSet()} will return 'false').
	 * 
	 * @param maxErrors
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
	 * @return
	 */
	@XmlTransient
	public Properties getProperties() {
		return properties;
	}


	/**
	 * Validation profile to used when checking/reporting issues.
	 * 
	 * @return the profile
	 */
	@XmlAttribute(required=false)
	public String getProfile() {
		return profile;
	}


	/**
	 * Sets the validation profile to use when checking/reporting issues.
	 * 
	 * @param profile the profile name
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
	 * @param obj
	 * @return
	 */
	public String identify(Object obj) {
		return idCalc.identify(obj);
	}
} 