package org.biopax.validator;

/**
 * Validation rule behavior:
 * 
 *       IGNORE -- skip: do not validate, nor report any messages;
 *       FIXIT -- on errors, fix the model or recommend how; log the message;
 *       ERROR -- report as error, fail if error limit is exceeded;
 *       WARNING -- report as warning.
 *
 * @author rodche
 */
public enum Behavior 
{
	IGNORE,  // a rule does not check;
	FIXIT,   // reports as error, trying to fix;
	ERROR,   // reports as error;
	WARNING; // reports as warning;
}
