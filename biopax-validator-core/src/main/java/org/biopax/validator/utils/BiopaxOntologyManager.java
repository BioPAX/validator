package org.biopax.validator.utils;

import java.io.File;
import java.io.IOException;
import java.util.*;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.validator.impl.AbstractCvRule;
import org.biopax.validator.impl.CvTermRestriction;
import org.biopax.validator.impl.CvTermRestriction.UseChildTerms;
import org.springframework.core.io.Resource;

import psidev.ontology_manager.Ontology;
import psidev.ontology_manager.OntologyTermI;
import psidev.ontology_manager.impl.OntologyLoaderException;
import psidev.ontology_manager.impl.OntologyManagerContext;
import psidev.ontology_manager.impl.OntologyManagerImpl;
import psidev.ontology_manager.impl.OntologyUtils;

/**
 * Access to BioPAX controlled vocabularies.
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
public class BiopaxOntologyManager extends OntologyManagerImpl {
	private final static Log log = LogFactory.getLog(BiopaxOntologyManager.class);
	
	/**
	 * Constructor
	 * 
	 * @param ontologiesConfigXml
	 * @throws IOException 
	 * @throws OntologyLoaderException 
	 */
	public BiopaxOntologyManager(Resource ontologiesConfigXml, String ontDir)
	{
		if(ontDir != null) {
			File dir = new File(ontDir);
			if(!dir.exists()) {
				dir.mkdir();
			} else if(!dir.isDirectory() || !dir.canWrite()) {
				throw new RuntimeException("Is not a directory name or not writable : " + ontDir);
			}
			OntologyManagerContext.getInstance().setOntologyDirectory(dir);
		}
		
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
		Ontology ontologyAccess = getOntology(restriction.getOntologyId());
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
