package org.biopax.validator;

/**
 * Validation Profiles Type
 * 
 * This is to set several rules behavior at once
 * 
 * @author rodch
 *
 */
public enum Profile {
	INFO,	// to kindly report all errors and warnings
	STRICT,	// some rules tune warnings to errors
	FATAL	// only report severe errors (ignore others and warnings)
}
