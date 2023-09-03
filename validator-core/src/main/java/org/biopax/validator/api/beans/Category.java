package org.biopax.validator.api.beans;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;

/**
 * Validation error category.
 */
@XmlType(name="Category")
@XmlEnum
public enum Category 
{
	SYNTAX,			// XML/RDF/OWL (parsing) error
	SPECIFICATION,	// (or standard) - BioPAX OWL specification error
	RECOMMENDATION,	// something does not follow the BioPAX best practice
	INFORMATION;	// other
}