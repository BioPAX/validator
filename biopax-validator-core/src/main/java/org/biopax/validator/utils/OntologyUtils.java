package org.biopax.validator.utils;

import java.io.File;
import java.io.IOException;
import java.util.*;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.validator.impl.CvTermRestriction;
import org.biopax.validator.impl.CvTermRestriction.UseChildTerms;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.util.ResourceUtils;

import uk.ac.ebi.ook.web.services.Query;
import uk.ac.ebi.ook.web.services.client.QueryServiceFactory;


//TODO implement/add terms cache
public class OntologyUtils {
	private final static Log log = LogFactory.getLog(OntologyUtils.class);
	
	private Query ontologyQuery;
	
	public OntologyUtils(String url) throws Exception {
		ontologyQuery = QueryServiceFactory.getQueryService(url, "OLS");
	}
	
	
	/**
	 * Gets a restricted set of ontology(-ies) terms 
	 * (i.e., names including synonyms) that satisfy
	 * all the restrictions.
	 * 
	 * @param restrictions - objects that specify required ontology terms
	 * @return set of names (strings)
	 */
	public Set<String> getValidTermNames(Collection<CvTermRestriction> restrictions) {
		Set<String> terms = new HashSet<String>();
		
		// first, collect all the valid terms
		for(CvTermRestriction restriction : restrictions) {
			if(!restriction.isNot()) {
				Collection<String> names = getTermNames(restriction);
				for(String name : names) {
					terms.add(name);
				}
			}
		}
		
		// now remove all those where restriction 'not' property set to true
		for(CvTermRestriction restriction : restrictions) {
			if(restriction.isNot()) {
				Collection<String> names = getTermNames(restriction);
				for(String name : names) {
					terms.remove(name);
				}
			}
		}
		
		return terms;
	}
		

	/**
	 * Similar to getValidTermNames method, 
	 * but the term names in the result set are all in lower case.
	 * 
	 * @see #getValidTermNames(Collection<CvTermRestriction>)
	 * 
	 * @param restrictions
	 * @return
	 */
	public Set<String> getValidTermNamesLowerCase(Collection<CvTermRestriction> restrictions) {
		Set<String> names = new HashSet<String>();
		for(String name : getValidTermNames(restrictions)) {
			names.add(name.toLowerCase());
		}
		return names;
	}
	
	
	/**
	 * Gets term names and synonyms, using the 
	 * restriction bean to filter the whole ontology
	 * (restriction's 'not' property is ignored)
	 * 
	 * @param restriction
	 * @return
	 */
	public Collection<String> getTermNames(CvTermRestriction restriction) {
		Set<String> names = new HashSet<String>();
		
		String ontologyId = restriction.getOntologyId();
		String term = ontologyQuery.getTermById(restriction.getId(), ontologyId);
		if(term == null) {
			log.error("Cannot Get " + restriction.getOntologyId()
					+ " Ontology Term for the Accession: " + restriction.getId());
			return names;
		}
		
		if(restriction.isTermAllowed()) {
			names.add(term);
		}
		if (restriction.getChildrenAllowed() == UseChildTerms.ALL) {
			names.addAll(ontologyAccess.getAllChildren(term));
		} else if (restriction.getChildrenAllowed() == UseChildTerms.DIRECT) {
			names.addAll(ontologyAccess.getDirectChildren(term));
		}
		
		// FIX xml escape symbols that come from the OntologyManager (a bug?)
		Collection<String> originalNames = OntologyUtils.getTermNames(terms);
		for(String name : originalNames) {
			names.add(StringEscapeUtils.unescapeXml(name));
		}
		
		return names;
	}
}
