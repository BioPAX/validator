package org.biopax.validator.result;

import java.io.Serializable;
import java.util.*;
import javax.xml.bind.annotation.*;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ValidatorResponse implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private List<Validation> validationResult;

	public ValidatorResponse() {
		validationResult = new ArrayList<Validation>();
	}

	/**
	 * List of error types; each item has unique 
	 * 
	 * @return
	 */
	public List<Validation> getValidationResult() {
		return validationResult;
	}

	public void setValidationResult(List<Validation> validationResult) {
		this.validationResult = validationResult;
	}

	public String toString() {
		StringBuffer result = new StringBuffer(
		super.toString());
		result.append("(results count: ");
		result.append(validationResult.size());
		result.append(')');
		return result.toString();
	}
	
	/**
	 * Add new result.
	 * 
	 * @param result
	 */
	public void addResult (Validation result) {
		this.validationResult.add(result);
	}

} 