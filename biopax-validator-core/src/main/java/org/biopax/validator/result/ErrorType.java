package org.biopax.validator.result;

import java.io.Serializable;
import java.util.*;

import javax.xml.bind.annotation.*;

@XmlType(name="ErrorType", namespace="http://biopax.org/validator/2.0/schema")
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class ErrorType implements Serializable {
	
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

	public ErrorType() {
		errorCase = new TreeSet<ErrorCaseType>();
	}

	public ErrorType(String code, Behavior type) {
		this();
		this.code = code.toLowerCase();
		this.type = type;
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
		StringBuffer result = new StringBuffer();
		result.append(type);
		result.append(" ");
		result.append(code);
		return result.toString();
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
	 * Total number of problems registered.
	 * 
	 * @return
	 */
	@XmlAttribute
	public int getTotalErrorCases() {
		return countErrors(null, null);
	}
	
	
	/**
	 * Count current error cases.
	 * 
	 * @param forObject if 'null', count everything
	 * @param reportedBy if 'null', everything's counted
	 * @return
	 */
	public int countErrors(String forObject, String reportedBy) {
		int count = 0;
		
		for(ErrorCaseType ec: errorCase) {
			if(forObject != null && !forObject.equals(ec.getObject())) {
				continue;
			}
			
			if(reportedBy != null && !reportedBy.equals(ec.getReportedBy())) {
				continue;
			}
			
			if(!ec.isFixed())
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
