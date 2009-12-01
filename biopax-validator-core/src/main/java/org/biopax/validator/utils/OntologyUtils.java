package org.biopax.validator.utils;

import java.rmi.RemoteException;
import java.util.*;

import javax.annotation.PreDestroy;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.validator.impl.AbstractCvRule;
import org.biopax.validator.impl.CvTermRestriction;
import org.biopax.validator.impl.CvTermRestriction.UseChildTerms;

import com.opensymphony.oscache.base.NeedsRefreshException;
import com.opensymphony.oscache.general.GeneralCacheAdministrator;

import uk.ac.ebi.ook.web.services.Query;
import uk.ac.ebi.ook.web.services.client.QueryServiceFactory;

/**
 * Access to biological controlled vocabularies 
 * via OLS (EBI) and local cache for particular 
 * validation rules (CV rules and xrefHelper).
 * 
 * THIS REQUIRES INTERNET CONNECTION 
 * (unless the cache was created in previous runs)
 * 
 * @author rodche
 *
 */
public class OntologyUtils {
	private final static Log log = LogFactory.getLog(OntologyUtils.class);
	
	private Query ontologyQuery;
	
	 // cache pairs <String, Set<String>> (CVrule name, set of valid terms) in file system
	private GeneralCacheAdministrator cacheAdministrator;
	
	public OntologyUtils(String url, GeneralCacheAdministrator cacheAdministrator) 
		throws Exception 
	{
		this.ontologyQuery = QueryServiceFactory.getQueryService(url, "OLS");
		this.cacheAdministrator = cacheAdministrator;
	}
	
	/**
	 * Gets the Osache cache administrator.
	 * 
	 * @return
	 */
	public GeneralCacheAdministrator getCacheAdministrator() {
		return cacheAdministrator;
	}
	
	/**
	 * Gets valid ontology terms (and synonyms)
	 * using the constraints from the rule bean.
	 * 
	 * @param cvRule
	 * @return
	 */
	public Set<String> getValidTerms(AbstractCvRule<?> cvRule) {
		try {
			return (Set<String>) cacheAdministrator.getFromCache(cvRule.getName());
		} catch (NeedsRefreshException e) {
			if(log.isDebugEnabled())
				log.debug("Now re-bulding the set of valid CV terms for " 
						+ cvRule.getName() + "...");
		}

		Set<String> terms = getValidTermNamesLowerCase(cvRule.getRestrictions());
		boolean saved = false;
		try {
			cacheAdministrator.putInCache(cvRule.getName(), terms);
			cacheAdministrator.flushEntry(cvRule.getName());
			saved = true;
		} finally {
			if(!saved) {
				cacheAdministrator.cancelUpdate(cvRule.getName());
			}
		}
		return terms;
	}
	
	
	/**
	 * Gets a restricted set of ontology(-ies) terms 
	 * (i.e., names including synonyms) that satisfy
	 * all the restrictions.
	 * 
	 * @param restrictions - objects that specify required ontology terms
	 * @return set of names (strings)
	 */
	private Set<String> getValidTermNames(Collection<CvTermRestriction> restrictions) {
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
	 * Gets term names and synonyms using the 
	 * restriction bean to filter the data.
	 * (restriction's 'NOT' property is ignored here)
	 * 
	 * @param restriction
	 * @return
	 */
	public Set<String> getTermNames(CvTermRestriction restriction) {
		Set<String> terms = new HashSet<String>();
		
		String ontology = restriction.getOntologyId();
		String accession = restriction.getId();
		String term = null;
		
		try {
			term = ontologyQuery.getTermById(accession, ontology);
		} catch (RemoteException e) {
			throw new BiopaxValidatorException(e);
		}
		
		if(term == null || term.length() == 0 
				|| term.equals( accession ) ) {
			throw new BiopaxValidatorException(
				"It did not find the term name using " +
				restriction.getOntologyId() + " Ontology " 
				+ " and the " + restriction.getId());
		}
		
		// add this term name and synonyms if it's allowed
		if(restriction.isTermAllowed()) {
			terms.add(StringEscapeUtils.unescapeXml(term));
			fetchSynonyms(accession, ontology, terms);
		}
		
		if (restriction.getChildrenAllowed() == UseChildTerms.ALL) {
			fetchAllChildren(accession, ontology, terms);
		} else if (restriction.getChildrenAllowed() == UseChildTerms.DIRECT) {
			fetchDirectChildren(accession, ontology, terms);
		}

		return terms;
	}
	
	/**
	 * Gets CV term's direct children names and synonyms
	 * 
	 * @param accession
	 * @param ontologyId
	 * @param dest
	 */
	public void fetchDirectChildren(String accession, String ontologyId, Set<String> dest) {
		fetchChildren(accession, ontologyId, 1, dest);
	}

	/**
	 * Gets CV term's all children names and synonyms
	 * 
	 * @param accession
	 * @param ontologyId
	 * @param dest
	 */
	void fetchAllChildren(String accession, String ontologyId, Set<String> dest) {
		fetchChildren(accession, ontologyId, -1, dest);
	}
	

	private void fetchChildren(String accession, String ontologyId, int level, Set<String> dest) {
		final int[] relationshipTypes = {1, 2, 3, 4};
		Map kids = new HashMap();
		try {
			kids = ontologyQuery.getTermChildren(accession, ontologyId, 
					level, relationshipTypes);			
		} catch (RemoteException e) {
			throw new BiopaxValidatorException(e);
		}
		
		// also add synonyms
        for ( Object o : kids.keySet() ) {
            Object v = kids.get( o ); // get name by accession
            if ( o instanceof String && v instanceof String ) {
                fetchSynonyms((String)o, ontologyId, dest);
                dest.add(StringEscapeUtils.unescapeXml((String)v));
            } else {
                throw new IllegalStateException( "OLS query returned unexpected result!" +
                            " Expected Map with key and value of class String," +
                            " but found key class: " + o.getClass().getName() +
                            " and value class: " + v.getClass().getName() );
            }
        }
	}
	
	/**
	 * Gets term synonyms from its metadata
	 * 
	 * @param accession
	 * @param ontologyID
	 * @param dest
	 */
	public void fetchSynonyms(String accession, String ontologyID,
			Set<String> dest) {
		Map metadata = null;
		try {
			metadata = ontologyQuery.getTermMetadata(accession, ontologyID);
		} catch (Exception e) {
			log.warn("Error while loading term synonyms from OLS "
					+ "for term: " + accession, e);
			return;
		}

		for (Object k : metadata.keySet()) {
			final String key = (String) k;
			// That's the only way OLS provides synonyms, all keys are different
			// so we are fishing out keywords :(
			if (key != null
					&& (key.contains("synonym") || key
							.contains("Alternate label"))) {
				String value = (String) metadata.get(k);
				if (value != null) {
					dest.add(StringEscapeUtils.unescapeXml(value.trim()));
				}
			}
		}
	}
    
    /**
     * Flushes CV terms cache
     */
    @PreDestroy
    public void flushCache() {
    	cacheAdministrator.flushAll();
    }
    
}
