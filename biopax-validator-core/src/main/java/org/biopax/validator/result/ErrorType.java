package org.biopax.validator.result;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;

import javax.xml.bind.annotation.*;

@XmlType(name="ErrorType", namespace="http://biopax.org/validator/2.0/schema")
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class ErrorType implements Serializable, Comparable<ErrorType> {
	
	private static final long serialVersionUID = 1L;
	
	/* 
	 * Collection of error cases 
	 * must have unique error types (identified by code).
	 * This is to be assured by implementing it as HashMap
	 * and overriding the 'equals' method. 
	 * 
	 */
	private final Set<ErrorCaseType> errorCase; 
	private String code = null;
	private String message = null;
	private Behavior type = Behavior.ERROR; // default
	private Category category = Category.INFORMATION; // default

	public ErrorType() {
		errorCase = new ConcurrentSkipListSet<ErrorCaseType>(); //new TreeSet<ErrorCaseType>();
	}

	public ErrorType(String code, Behavior type) {
		this();
		this.code = code.toLowerCase();
		this.type = type;
	}
	
	@XmlAttribute
	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}
	
	public Collection<ErrorCaseType> getErrorCase() {
		return errorCase;
	}

	public void setErrorCase(Collection<ErrorCaseType> errorCases) {
		this.errorCase.clear();
		this.errorCase.addAll(errorCases);
	}
	
	@XmlAttribute
	public String getCode() {
		return code;
	}

	public void setCode(String newCode) {
		code = newCode.toLowerCase();
	}

	@XmlAttribute
	public String getMessage() {
		return message;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}
	
	/**
	 * Adds (may be updates...) a error case;
	 * only the error message that happened last 
	 * (for the same object and rule) is kept
	 * (e.g., in a series of unsuccessful attempts 
	 * to set a biopax property, the last error message
	 * will override previous ones...)
	 * 
	 * @see ErrorCaseType#equals(Object)
	 * @see ErrorCaseType#hashCode()
	 * 
	 * @param newCase
	 */
	public void addErrorCase(ErrorCaseType newCase) {
		// errorCase.add(newCase);
		if (errorCase.contains(newCase)) //
		{
			for (ErrorCaseType ect : errorCase) {
				if(ect.equals(newCase)) {
					// - found by object and rule id;
					// update the existing case
					ect.setFixed(newCase.isFixed());
					
					if(!newCase.isFixed()) {
						// new message (error re-occur)
						ect.setMessage(newCase.getMessage());
					} else {
						// message ignored (previous error is being fixed)
					}
				}
			}
		} else {
			errorCase.add(newCase);
		}
	}
	
	/**
	 * Adds error cases
	 * 
	 * @param cases
	 */
	public void addCases(Collection<ErrorCaseType> cases) {
		//this.errorCase.addAll(cases);
		for (ErrorCaseType errorCase : cases) {
			addErrorCase(errorCase);
		}
	}
	
	
	public void removeErrorCase(ErrorCaseType eCase) {
		errorCase.remove(eCase);
	}
	
	@Override
	public String toString() {
		return type + " " + code; 
	}
	
	@Override
	public boolean equals(Object obj) {	
		if(obj instanceof ErrorType) {
			ErrorType et = (ErrorType) obj;
			return et.toString().equalsIgnoreCase(toString());
		} 
		return false;
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}
	
	public int compareTo(ErrorType et) {
		return this.toString().compareToIgnoreCase(et.toString());
	}
	
	@XmlAttribute
	public Behavior getType() {
		return type;
	}
	
	public void setType(Behavior type) {
		this.type = type;
	}
	
	/**
	 * Total number of cases registered, including fixed.
	 * 
	 * @return
	 */
	@XmlAttribute
	public int getTotalCases() {
		return countErrors(null, null, false);
	}
	
	
	/**
	 * Total number of cases not fixed yet.
	 * 
	 * @return
	 */
	@XmlAttribute
	public int getNotFixedCases() {
		return countErrors(null, null, true);
	}
	
	
	/**
	 * Count current error cases.
	 * 
	 * @param forObject if 'null', count everything
	 * @param reportedBy if 'null', everything's counted
	 * @param ignoreFixed skip fixed if true
	 * @return
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
	 * @param searchBy
	 * @return
	 */
	public ErrorCaseType findErrorCase(final ErrorCaseType searchBy) {
		if(getErrorCase().contains(searchBy)) {
			for (ErrorCaseType ec : getErrorCase()) {
				if (ec.equals(searchBy)) {
					return ec;
				}
			}
		}
		return null;
	}
	
	
}
