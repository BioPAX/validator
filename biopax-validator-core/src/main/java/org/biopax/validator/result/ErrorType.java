package org.biopax.validator.result;

import java.io.Serializable;
import java.util.*;

import javax.xml.bind.annotation.*;

import org.biopax.validator.Behavior;

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
	final Collection<ErrorCaseType> errorCase; 
	String code = null;
	String message = null;
	Behavior type = Behavior.ERROR; // default

	public ErrorType() {
		errorCase = new HashSet<ErrorCaseType>();
	}

	public ErrorType(String code, Behavior type) {
		this();
		this.code = code.toLowerCase();
		this.type = type;
	}
	
	public Collection<? extends ErrorCaseType> getErrorCase() {
		List<ErrorCaseType> list = new ArrayList<ErrorCaseType>(errorCase);
		Collections.sort(list);
		return list;
	}

	public void setErrorCase(Collection<? extends ErrorCaseType> errorCases) {
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
	 * Adds a new error case,
	 * 
	 * @param newCase
	 */
	public void addErrorCase(ErrorCaseType newCase) {
		errorCase.add(newCase);
	}
	
	public void addCases(Collection<? extends ErrorCaseType> cases) {
		this.errorCase.addAll(cases);
	}
	
	
	public void removeErrorCase(ErrorCaseType eCase) {
		errorCase.remove(eCase);
	}
	
	@Override
	public String toString() {
		StringBuffer result = new StringBuffer(); // (super.toString());
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
			count++;
		}
		
		return count;
	}
}
