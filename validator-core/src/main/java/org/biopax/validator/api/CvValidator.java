package org.biopax.validator.api;

/*
 *
 */

import java.util.Collection;
import java.util.Set;

import org.biopax.psidev.ontology_manager.OntologyTermI;


public interface CvValidator {

	/**
	 * Gets valid ontology term names
	 * using the constraints from the rule bean.
	 * 
	 * @param cvRule
	 * @return
	 */
	Set<String> getValidTermNames(CvRule<?> cvRule);

	/**
	 * Gets valid ontology terms
	 * using the constraints from the rule bean.
	 * 
	 * @param cvRule
	 * @return a set of ontology terms (beans)
	 */
	Set<OntologyTermI> getValidTerms(CvRule<?> cvRule);

	/**
	 * Gets the set of terms (including synonyms) 
	 * that satisfy all the restrictions.
	 * 
	 * @param restrictions - objects that specify required ontology terms
	 * @return set of names (strings)
	 */
	Set<String> getValidTermNames(Collection<CvRestriction> restrictions);

	/**
	 * Similar to getValidTermNames method, 
	 * but the term names in the result set are all in lower case.
	 * 
	 * @see #getValidTermNames(Collection)
	 * 
	 * @param restrictions
	 * @return
	 */
	Set<String> getValidTermNamesLowerCase(
			Collection<CvRestriction> restrictions);

	/**
	 * Gets term names and synonyms using the 
	 * restriction bean to filter the data.
	 * (restriction's 'NOT' property is ignored here)
	 * 
	 * @param restriction
	 * @return
	 */
	Set<String> getTermNames(CvRestriction restriction);

	/**
	 * Gets a restricted set of CVs 
	 * (including for synonyms) that satisfy
	 * all the restrictions in the set.
	 * 
	 * @param restrictions - set of beans that together define the required constraint
	 * @return set of ontology terms
	 */
	Set<OntologyTermI> getValidTerms(Collection<CvRestriction> restrictions);

	/**
	 * Gets CVs (including for synonyms) using the 
	 * criteria defined by the bean
	 * ('NOT' property, if set 'true', is ignored)
	 * 
	 * @param restriction
	 * @return
	 */
	Set<OntologyTermI> getTerms(CvRestriction restriction);

}