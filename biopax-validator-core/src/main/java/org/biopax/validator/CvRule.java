package org.biopax.validator;

import java.util.Set;

import org.biopax.validator.impl.CvTermRestriction;

public interface CvRule<T> extends Rule<T>{

	/**
	 * Gets valid CV terms (that obey the restrictions)
	 * @return
	 */
	Set<String> getValidTerms();

	/**
	 * Sets valid terms for this CV rule.
	 * @param validTerms
	 */
	void setValidTerms(Set<String> validTerms);

	Set<CvTermRestriction> getRestrictions();

	Class<T> getDomain();

	String getProperty();

}