package org.biopax.validator.result;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

/**
 * Categories of error
 * 
 * @author rodche
 */
@XmlType(name="Category", namespace="http://biopax.org/validator/2.0/schema")
@XmlEnum
public enum Category 
{
	SYNTAX,			// XML/RDF/OWL (parsing) error
	SPECIFICATION,	// (or standard) - BioPAX OWL specification error
	RECOMMENDATION,	// something does not follow the BioPAX best practice
	INFORMATION;	// other
}