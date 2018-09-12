package org.biopax.validator.api.beans;

/*
 *
 */

import java.io.Serializable;
import java.util.*;
import javax.xml.bind.annotation.*;


@XmlRootElement
@XmlType(name="ValidatorResponse")
@XmlAccessorType(XmlAccessType.FIELD)
public class ValidatorResponse implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@XmlElement
	private List<Validation> validation;

	public ValidatorResponse() {
		validation = new ArrayList<Validation>();
	}

	public List<Validation> getValidationResult() {
		return validation;
	}

	public void setValidationResult(List<Validation> validation) {
		this.validation = validation;
	}

	public String toString() {
		StringBuffer result = new StringBuffer(
		super.toString());
		result.append("(results count: ");
		result.append(validation.size());
		result.append(')');
		return result.toString();
	}
	
	public void addValidationResult (Validation result) {
		this.validation.add(result);
	}

} 