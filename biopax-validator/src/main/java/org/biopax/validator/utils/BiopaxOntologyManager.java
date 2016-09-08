package org.biopax.validator.utils;

import java.util.*;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.psidev.ontology_manager.OntologyAccess;
import org.biopax.psidev.ontology_manager.OntologyTermI;
import org.biopax.psidev.ontology_manager.impl.OntologyManagerImpl;
import org.biopax.validator.api.CvRestriction;
import org.biopax.validator.api.CvRule;
import org.biopax.validator.api.CvValidator;
import org.biopax.validator.api.CvRestriction.UseChildTerms;


/**
 * Access to BioPAX controlled vocabularies.
 * This component is built from a modified PSIDEV tool, OntologyAccess Manager, 
 * by extending it and adding several "proxy" methods that allow to extract 
 * validator-specific data only once and free the memory after it, if required.
 * However, it does not hide base class's methods.
 * 
 * REQUIRES INTERNET CONNECTION (when configured to use OBO URLs)
 * 
 * @author rodche
 *
 */
public class BiopaxOntologyManager extends OntologyManagerImpl implements CvValidator {
	private final static Log log = LogFactory.getLog(BiopaxOntologyManager.class);
	
	/**
	 * Constructor
	 * @param ontologiesConfig OBO ontologies configuration properties
	 * 
	 */
	public BiopaxOntologyManager(Properties ontologiesConfig)
	{
		try {
			loadOntologies(ontologiesConfig);
		} catch (Throwable e) {
			throw new RuntimeException("Failed to load or parse all required biological ontologies!", e);
		}
	}

	/* (non-Javadoc)
	 * @see org.biopax.validator.utils.CvValidator#getValidTermNames(org.biopax.validator.CvRule)
	 */
	public Set<String> getValidTermNames(CvRule<?> cvRule) {
		return getValidTermNamesLowerCase(cvRule.getRestrictions());
	}
	
	/* (non-Javadoc)
	 * @see org.biopax.validator.utils.CvValidator#getValidTerms(org.biopax.validator.CvRule)
	 */
	public Set<OntologyTermI> getValidTerms(CvRule<?> cvRule) {
		return getValidTerms(cvRule.getRestrictions());
	}
	
	
	/* (non-Javadoc)
	 * @see org.biopax.validator.utils.CvValidator#getValidTermNames(java.util.Collection)
	 */
	public Set<String> getValidTermNames(Collection<CvRestriction> restrictions) {
		Set<String> names = new HashSet<String>();
		Set<OntologyTermI> terms = getValidTerms(restrictions);
		names.addAll(getTermNames(terms));
		return names;
	}
		

	/* (non-Javadoc)
	 * @see org.biopax.validator.utils.CvValidator#getValidTermNamesLowerCase(java.util.Collection)
	 */
	public Set<String> getValidTermNamesLowerCase(Collection<CvRestriction> restrictions) {
		Set<String> names = new HashSet<String>();
		for(String name : getValidTermNames(restrictions)) {
			names.add(name.toLowerCase());
		}
		return names;
	}
	
	
	/* (non-Javadoc)
	 * @see org.biopax.validator.utils.CvValidator#getTermNames(org.biopax.validator.impl.CvTermRestriction)
	 */
	public Set<String> getTermNames(CvRestriction restriction) {
		Set<OntologyTermI> terms = getTerms(restriction);
		Set<String> names = new HashSet<String>();
		names.addAll(getTermNames(terms));
		return names;
	}
   
	
	
	/* (non-Javadoc)
	 * @see org.biopax.validator.utils.CvValidator#getValidTerms(java.util.Collection)
	 */
	public Set<OntologyTermI> getValidTerms(Collection<CvRestriction> restrictions) {
		Set<OntologyTermI> terms = new HashSet<OntologyTermI>();
		
		// first, collect all the valid terms
		for(CvRestriction restriction : restrictions) {
			if(!restriction.isNot()) {
				terms.addAll(getTerms(restriction));
			}
		}
		
		// now remove all those where restriction 'not' property set to true
		for(CvRestriction restriction : restrictions) {
			if(restriction.isNot()) {
				terms.removeAll(getTerms(restriction));
			}
		}
		
		return terms;
	}
	
	
	/* (non-Javadoc)
	 * @see org.biopax.validator.utils.CvValidator#getTerms(org.biopax.validator.impl.CvTermRestriction)
	 */
	public Set<OntologyTermI> getTerms(CvRestriction restriction) {
		Set<OntologyTermI> terms = new HashSet<OntologyTermI>();
		OntologyAccess ontologyAccess = getOntology(restriction.getOntologyId());
		if(ontologyAccess == null) {
			throw new IllegalArgumentException(
					"Cannot get access to the ontology using ID: " 
					+ restriction.getOntologyId() +
					"; I know the following IDs: " 
					+ getOntologyIDs().toString());
		}
		OntologyTermI term = ontologyAccess.getTermForAccession(restriction.getId());
		if(term == null) {
			log.error("Cannot Get " + restriction.getOntologyId()
					+ " OntologyAccess Term for the Accession: " + restriction.getId());
			return terms;
		}
		
		if(restriction.isTermAllowed()) {
			terms.add(term);
		}
		
		if (restriction.getChildrenAllowed() == UseChildTerms.ALL) {
			terms.addAll(ontologyAccess.getAllChildren(term));
		} else if (restriction.getChildrenAllowed() == UseChildTerms.DIRECT) {
			terms.addAll(ontologyAccess.getDirectChildren(term));
		}
		
		// FIX xml escape symbols that come from the OntologyManagerImpl (a bug?)
		for(OntologyTermI t : terms) {
			t.setPreferredName(StringEscapeUtils.unescapeXml(t.getPreferredName()));
			Set<String> synonyms = new HashSet<String>();
			for(String s : t.getNameSynonyms()) {
				synonyms.add(StringEscapeUtils.unescapeXml(s));
			}
			t.setNameSynonyms(synonyms);
		}
		
		return terms;
	}

}
