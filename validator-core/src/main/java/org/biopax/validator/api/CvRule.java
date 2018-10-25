package org.biopax.validator.api;


import java.util.Set;


/**
 * A controlled vocabulary (ontology) term and context rule for some object property.
 *
 * @param <T> object type to check
 */
public interface CvRule<T> extends Rule<T>{

	/**
	 * Gets valid CV terms (that obey the restrictions)
	 * @return recommended CV terms
	 */
	Set<String> getValidTerms();

	/**
	 * Sets valid terms for this CV rule.
	 * @param validTerms valid/recommended controlled vocabulary terms
	 */
	void setValidTerms(Set<String> validTerms);

	Set<CvRestriction> getRestrictions();

	Class<T> getDomain();

	String getProperty();

}