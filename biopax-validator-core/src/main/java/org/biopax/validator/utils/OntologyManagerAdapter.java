package org.biopax.validator.utils;

import java.io.IOException;
import java.util.*;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.validator.impl.AbstractCvRule;
import org.biopax.validator.impl.CvTermRestriction;
import org.biopax.validator.impl.CvTermRestriction.UseChildTerms;
import org.springframework.core.io.Resource;

import psidev.psi.tools.ontology_manager.OntologyManager;
import psidev.psi.tools.ontology_manager.OntologyManagerContext;
import psidev.psi.tools.ontology_manager.OntologyUtils;
import psidev.psi.tools.ontology_manager.impl.local.OntologyLoaderException;
import psidev.psi.tools.ontology_manager.interfaces.OntologyAccess;
import psidev.psi.tools.ontology_manager.interfaces.OntologyTermI;

/**
 * Access to biological controlled vocabularies.
 * This component is built from a modified PSIDEV tool, Ontology Manager, 
 * by extending it and adding several "proxy" methods that allow to extract 
 * validator-specific data only once and free the memory after it, if required.
 * However, it does not hide base class's methods.
 * 
 * REQUIRES INTERNET CONNECTION (when configured to use OBO URLs)
 * 
 * @author rodche
 *
 */
public class OntologyManagerAdapter extends OntologyManager {
	private final static Log log = LogFactory.getLog(OntologyManagerAdapter.class);
	
	/**
	 * Constructor
	 * 
	 * @param ontologiesConfigXml
	 * @throws IOException 
	 * @throws OntologyLoaderException 
	 */
	public OntologyManagerAdapter(Resource ontologiesConfigXml)
	{
		OntologyManagerContext.getInstance().setStoreOntologiesLocally(true); // to work fast!
		try {
			loadOntologies(ontologiesConfigXml.getInputStream());
		} catch (OntologyLoaderException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}	
	}

	/**
	 * Gets valid ontology term names
	 * using the constraints from the rule bean.
	 * 
	 * @param cvRule
	 * @return
	 */
	public Set<String> getValidTermNames(AbstractCvRule<?> cvRule) {
		return getValidTermNamesLowerCase(cvRule.getRestrictions());
	}
	
	/**
	 * Gets valid ontology terms
	 * using the constraints from the rule bean.
	 * 
	 * @param cvRule
	 * @return a set of ontology terms (beans)
	 */
	public Set<OntologyTermI> getValidTerms(AbstractCvRule<?> cvRule) {
		return getValidTerms(cvRule.getRestrictions());
	}
	
	
	/**
	 * Gets the set of terms (including synonyms) 
	 * that satisfy all the restrictions.
	 * 
	 * @param restrictions - objects that specify required ontology terms
	 * @return set of names (strings)
	 */
	public Set<String> getValidTermNames(Collection<CvTermRestriction> restrictions) {
		Set<String> names = new HashSet<String>();
		Set<OntologyTermI> terms = getValidTerms(restrictions);
		names.addAll(OntologyUtils.getTermNames(terms));
		return names;
	}
		

	/**
	 * Similar to getValidTermNames method, 
	 * but the term names in the result set are all in lower case.
	 * 
	 * @see #getValidTermNames(Collection)
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
	 * Gets term names and synonyms using the 
	 * restriction bean to filter the data.
	 * (restriction's 'NOT' property is ignored here)
	 * 
	 * @param restriction
	 * @return
	 */
	public Set<String> getTermNames(CvTermRestriction restriction) {
		Set<OntologyTermI> terms = getTerms(restriction);
		Set<String> names = new HashSet<String>();
		names.addAll(OntologyUtils.getTermNames(terms));
		return names;
	}
   
	
	
	/**
	 * Gets a restricted set of CVs 
	 * (including for synonyms) that satisfy
	 * all the restrictions in the set.
	 * 
	 * @param restrictions - set of beans that together define the required constraint
	 * @return set of ontology terms
	 */
	public Set<OntologyTermI> getValidTerms(Collection<CvTermRestriction> restrictions) {
		Set<OntologyTermI> terms = new HashSet<OntologyTermI>();
		
		// first, collect all the valid terms
		for(CvTermRestriction restriction : restrictions) {
			if(!restriction.isNot()) {
				terms.addAll(getTerms(restriction));
			}
		}
		
		// now remove all those where restriction 'not' property set to true
		for(CvTermRestriction restriction : restrictions) {
			if(restriction.isNot()) {
				terms.removeAll(getTerms(restriction));
			}
		}
		
		return terms;
	}
	
	
	/**
	 * Gets CVs (including for synonyms) using the 
	 * criteria defined by the bean
	 * ('NOT' property, if set 'true', is ignored)
	 * 
	 * @param restriction
	 * @return
	 */
	public Set<OntologyTermI> getTerms(CvTermRestriction restriction) {
		Set<OntologyTermI> terms = new HashSet<OntologyTermI>();
		OntologyAccess ontologyAccess = getOntologyAccess(restriction.getOntologyId());
		OntologyTermI term = ontologyAccess.getTermForAccession(restriction.getId());
		if(term == null) {
			log.error("Cannot Get " + restriction.getOntologyId()
					+ " Ontology Term for the Accession: " + restriction.getId());
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
		
		// FIX xml escape symbols that come from the OntologyManager (a bug?)
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
	
	/**
	 * Search for terms using a name (synonym) name.
	 * The search is case insensitive.
	 * It iterates through all loaded ontologies, so use with caution!
	 * 
	 * @return
	 */
	public Set<OntologyTermI> searchTermByName(String name) {
		Set<OntologyTermI> found  = new HashSet<OntologyTermI>();
		
		for(String ontologyId: getOntologyIDs()) {
			OntologyAccess oa = getOntologyAccess(ontologyId);
			for(OntologyTermI term : oa.getOntology().getOntologyTerms()) {
				if(term.getPreferredName().equalsIgnoreCase(name)) {
					found.add(term);
				} else {
					for(String syn : term.getNameSynonyms()) {
						if(syn.equalsIgnoreCase(name)) {
							found.add(term);
						}
					}
				}
			}
		}
		
		return found;
	}
}
