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