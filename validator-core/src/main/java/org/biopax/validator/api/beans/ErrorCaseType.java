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
import javax.xml.bind.annotation.*;

@XmlType(name="ErrorCaseType")
@XmlAccessorType(XmlAccessType.FIELD)
public class ErrorCaseType implements Serializable, Comparable<ErrorCaseType> {

	private static final long serialVersionUID = 1L;
	
	@XmlElement
	protected String message = null;
	@XmlAttribute
	protected String object = "";
	@XmlAttribute
	protected String reportedBy = null;
	@XmlAttribute
	protected boolean fixed = false;
	
	public ErrorCaseType() {
	}
	
	public ErrorCaseType(String reportedBy, String object, String msg) {
		this.reportedBy = reportedBy;
		this.object = object;
		this.message = msg;
		this.fixed = false;
	}
	
	public String getMessage() {
		return message;
	}

	public void setMessage(String newMessage) {
		message = newMessage;
	}

	public String getObject() {
		return object;
	}

	public void setObject(String newObject) {
		object = newObject;
	}

	public String getReportedBy() {
		return reportedBy;
	}
	
	public void setReportedBy(String reportedBy) {
		this.reportedBy = reportedBy;
	}
	
	public boolean isFixed() {
		return fixed;
	}
	
	public void setFixed(boolean fixed) {
		this.fixed = fixed;
	}
	
	public String toString() {
		StringBuffer result = new StringBuffer();
		result.append("(obj: ");
		result.append(object);
		result.append(", by: ");
		result.append(reportedBy);
		result.append(", msg: ");
		result.append(message);
		result.append(')');
		return result.toString();
	}

	public int compareTo(ErrorCaseType o) {
		// preventive NullPointerError error fix:
		if(getObject()==null) {	this.object = "";}		
		return getObject().compareToIgnoreCase(o.getObject());
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof ErrorCaseType) {
			ErrorCaseType that = ((ErrorCaseType)obj);
			return object.equalsIgnoreCase(that.object) 
					&& reportedBy.equalsIgnoreCase(that.reportedBy);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		int result = 31 + (object != null ? object.hashCode() : 0);
		result = 31 * result + (reportedBy != null ? reportedBy.hashCode() : 0);
		return result;
	}
}
