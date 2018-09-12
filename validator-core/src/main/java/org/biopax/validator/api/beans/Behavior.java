package org.biopax.validator.api.beans;

/*
 *
 */

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
@XmlType(name="Behavior")
@XmlEnum
public enum Behavior 
{
	IGNORE,  // a rule does not check;
	WARNING, // reports as warning;
	ERROR;   // reports as error;
}