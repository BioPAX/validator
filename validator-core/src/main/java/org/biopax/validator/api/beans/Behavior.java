package org.biopax.validator.api.beans;


import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;

/**
 * Validation rule behavior:
 * 
 *       IGNORE -- skip: do not validate, nor report any messages;
 *       ERROR -- report as error, fail if error limit is exceeded;
 *       WARNING -- report as warning.
 */
@XmlType(name="Behavior")
@XmlEnum
public enum Behavior 
{
	IGNORE,  // a rule does not check;
	WARNING, // reports as warning;
	ERROR;   // reports as error;
}