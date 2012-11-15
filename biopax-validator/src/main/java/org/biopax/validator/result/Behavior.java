package org.biopax.validator.result;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

/**
 * Validation rule behavior:
 * 
 *       IGNORE -- skip: do not validate, nor report any messages;
 *       ERROR -- report as error, fail if error limit is exceeded;
 *       WARNING -- report as warning.
 *
 * @author rodche
 */
@XmlType(name="Behavior", namespace="http://biopax.org/validator/2.0/schema")
@XmlEnum
public enum Behavior 
{
	IGNORE,  // a rule does not check;
	WARNING, // reports as warning;
	ERROR;   // reports as error;
}