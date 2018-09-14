package org.biopax.validator.api.beans;


import java.io.Serializable;
import java.util.*;

import javax.xml.bind.annotation.*;

@XmlType(name="ErrorType")
@XmlAccessorType(XmlAccessType.FIELD)
public class ErrorType implements Serializable, Comparable<ErrorType> {
	
	private static final long serialVersionUID = 1L;
	
	/* 
	 * Collection of error cases 
	 * must have unique error types (identified by code).
	 * This is to be assured by implementing it as HashMap
	 * and overriding the 'equals' method. 
	 * 
	 */
	@XmlElement
	private final Set<ErrorCaseType> errorCase; 
	@XmlAttribute
	private String code = null;
	@XmlAttribute
	private String message = null;
	@XmlAttribute
	private Behavior type = Behavior.ERROR; // default
	@XmlAttribute
	private Category category = Category.INFORMATION; // default	
	@XmlAttribute
	private int notFixedCases = 0;
	@XmlAttribute
	private int totalCases = 0;

	public ErrorType() {
		errorCase = new TreeSet<ErrorCaseType>();
	}

	public ErrorType(String code, Behavior type) {
		this();
		this.code = code.toLowerCase();
		this.type = type;
	}
	
	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}
	
	/**
	 * Unmodifiable set of validation current error cases.
	 * 
	 * @return error cases
	 */
	public synchronized Collection<ErrorCaseType> getErrorCase() {
		return Collections.unmodifiableSet(errorCase);
	}

	public synchronized void setErrorCase(Collection<ErrorCaseType> errorCases) {
		errorCase.clear();
		errorCase.addAll(errorCases);
	}
	
	public String getCode() {
		return code;
	}

	public void setCode(String newCode) {
		code = newCode.toLowerCase();
	}

	public String getMessage() {
		return message;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}
	
	/**
	 * Adds (or updates) an error case;
	 * only the error message that happened last 
	 * (for the same object and rule) is kept
	 * (e.g., in a series of unsuccessful attempts 
	 * to set a biopax property, the last error message
	 * will override previous ones...)
	 * 
	 * @see ErrorCaseType#equals(Object)
	 * @see ErrorCaseType#hashCode()
	 * 
	 * @param newCase case
	 */
	public synchronized void addErrorCase(ErrorCaseType newCase) {
		ErrorCaseType ect = findErrorCase(newCase);
		if(ect != null) { //found a previously reported case
			ect.setFixed(newCase.fixed);
			if(!newCase.fixed) {		
				// update the message
				ect.setMessage(newCase.getMessage());
			}
		} else { //fresh error case
			errorCase.add(newCase);
		}
	}
	

	public void addCases(Collection<ErrorCaseType> cases) {
		for (ErrorCaseType errorCase : cases) {
			addErrorCase(errorCase);
		}
	}
		
	public synchronized void removeErrorCase(ErrorCaseType eCase) {
		errorCase.remove(eCase);
	}

	
	@Override
	public String toString() {
		return type + " " + code; 
	}
	
	@Override
	public boolean equals(Object obj) {	
		if(obj instanceof ErrorType) {
			ErrorType that = (ErrorType) obj;
			return type.equals(that.type)
					&& code.equals(that.code);
		} 
		return false;
	}
	
	@Override
	public int hashCode() {
		int result = 31 + (type != null ? type.hashCode() : 0);
		result = 31 * result + (code != null ? code.hashCode() : 0);
		return result;
	}
	
	public int compareTo(ErrorType et) {
		return this.toString().compareToIgnoreCase(et.toString());
	}
	
	public Behavior getType() {
		return type;
	}
	
	public void setType(Behavior type) {
		this.type = type;
	}
	
	/**
	 * Total number of error cases registered, including those auto-fixed.
	 * 
	 * @return number
	 */
	public int getTotalCases() {
		return totalCases;
	}
	public void setTotalCases(int n) {
		this.totalCases = n;
	}
	
	/**
	 * Total number of error cases not fixed yet.
	 * 
	 * @return number
	 */
	public int getNotFixedCases() {
		return notFixedCases;
	}
	public void setNotFixedCases(int n) {
		this.notFixedCases = n;
	}
	
	/**
	 * Count current error cases.
	 * 
	 * @param forObject if 'null', count everything
	 * @param reportedBy if 'null', everything's counted
	 * @param ignoreFixed skip fixed if true
	 * @return number
	 */
	public int countErrors(String forObject, String reportedBy, boolean ignoreFixed) {
		int count = 0;
		
		for(ErrorCaseType ec: errorCase) {
			if(forObject != null && !forObject.equals(ec.getObject())) {
				continue;
			}
			
			if(reportedBy != null && !reportedBy.equals(ec.getReportedBy())) {
				continue;
			}
			
			if(ignoreFixed == true && ec.isFixed()) {
				continue;
			}
			
			count++;
		}
		
		return count;
	}
	
	/**
	 * Returns the existing (happened) error case object by
	 * ErrorCaseType (new, not yet reported, or a copy).
	 * Note: 'equals' method is overridden in ErrorCaseType
	 * 
	 * @see ErrorCaseType#equals(Object) 
	 * 
	 * @param searchBy case type (as pattern) to match
	 * @return actual error case
	 */
	public synchronized ErrorCaseType findErrorCase(final ErrorCaseType searchBy) {
		if(errorCase.contains(searchBy)) {
			for (ErrorCaseType ec : getErrorCase()) {
				if (ec.equals(searchBy)) {
					return ec;
				}
			}
		}
		return null;
	}

}
