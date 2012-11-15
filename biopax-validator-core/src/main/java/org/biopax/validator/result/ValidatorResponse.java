package org.biopax.validator.result;

import java.io.Serializable;
import java.util.*;
import javax.xml.bind.annotation.*;


@XmlRootElement(namespace="http://biopax.org/validator/2.0/schema")
@XmlType(name="ValidatorResponse", namespace="http://biopax.org/validator/2.0/schema")
@XmlAccessorType(XmlAccessType.FIELD)
public class ValidatorResponse implements Serializable {

	private static final long serialVersionUID = 1L;
	
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