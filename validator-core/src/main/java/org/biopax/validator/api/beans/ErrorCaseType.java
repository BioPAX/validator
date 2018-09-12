package org.biopax.validator.api.beans;

/*
 *
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
