package org.biopax.validator;

/**
 * Validation rule behavior:
 * 
 *       IGNORE -- skip: do not validate, nor report any messages;
 *       ERROR -- report as error, fail if error limit is exceeded;
 *       WARNING -- report as warning.
 *
 * @author rodche
 */
public enum Behavior 
{
	IGNORE,  // a rule does not check;
	WARNING, // reports as warning;
	ERROR;   // reports as error;
}
