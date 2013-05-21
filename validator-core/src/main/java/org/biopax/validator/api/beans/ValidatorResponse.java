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
import javax.xml.bind.annotation.*;


@XmlRootElement
@XmlType(name="ValidatorResponse")
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