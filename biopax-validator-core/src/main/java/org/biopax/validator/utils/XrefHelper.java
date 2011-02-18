package org.biopax.validator.utils;

import java.util.*;
import java.util.regex.*;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Configurable;

import psidev.ontology_manager.Ontology;
import psidev.ontology_manager.OntologyTermI;
import psidev.ontology_manager.impl.OntologyTermImpl;

import org.biopax.miriam.MiriamLink;

/**
 * This helps validate xref's 'db', where different database synonymous 
 * or misspelled names are often used, and 'id' that should match the 
 * datasource's pattern.
 *
 * @author rodche
 */
@Configurable
public class XrefHelper {
    private static final Log log = LogFactory.getLog(XrefHelper.class);
    
    private Map<String, Pattern> dataPatterns;
    private Set<List<String>> synonyms;
    private BiopaxOntologyManager ontologyManager;
	
    
    public XrefHelper(Set<List<String>> extraSynonyms, 
    	BiopaxOntologyManager ontologyManager) 
    		throws Exception 
    {   	
    	
		// all database names and ID patterns go here
		this.dataPatterns = new HashMap<String, Pattern>();
		
		// add pre-configured synonyms, if any
		this.synonyms = (extraSynonyms != null && !extraSynonyms.isEmpty()) 
			? this.synonyms = extraSynonyms 
			: new HashSet<List<String>>();
		
		this.ontologyManager = ontologyManager;
    }

    @PostConstruct
    void init() {
		// adds names and assigns regexps from Miriam;
		// also makes those names primary synonyms
		for (String dt : MiriamLink.getDataTypesName()) {
			String db = dbName(dt); // consistent using dbName(name) is crucial (removes the case sensitivity)
			String regexp = MiriamLink.getDataTypePattern(dt);
			
			Pattern pattern = null;
			try {
				pattern= Pattern.compile(regexp);
			} catch (PatternSyntaxException e) {
				log.error(
					"Pattern compilation failed for MIRIAM " +
					"db=" + db + "; regexp=" + dt + "; " + e);
			}
			
			List<String> synonyms = new ArrayList<String>();
			synonyms.add(db);
			String[] names = MiriamLink.getNames(dt);
			if (names != null && names.length>0) {
				for(String name : names) {
					synonyms.add(dbName(name)); 
					// consistent using dbName(name) is crucial (removes the case sensitivity)
				}
			}
			addDb(true, synonyms);
			// set patterns for all synonyms
			for (String s : getSynonymsForDbName(db)) {
				dataPatterns.put(s, pattern);
			}
		}

		// load all names from MI 'database citation'
		Ontology mi = ontologyManager.getOntology("MI");
		Collection<OntologyTermI> terms = mi.getAllChildren(new OntologyTermImpl("MI:0444")); 
		for (OntologyTermI term : terms) {
			String db = dbName(term.getPreferredName());
			List<String> synonyms = new ArrayList<String>();
			synonyms.add(db);
			for(String name : term.getNameSynonyms()) {
				synonyms.add(dbName(name)); // consistent using dbName(name) is crucial (removes the case sensitivity)
			}
			addDb(false, synonyms);
		}	
		
		if(log.isDebugEnabled()) {
			log.debug("Synonyms for EntrezGene: "
				+ getSynonymsForDbName("EntrezGene").toString()
				+ "; prefered name: " + getPrimaryDbName("EntrezGene"));	
		}

		assert(!getSynonymsForDbName("EntrezGene").isEmpty());		
    }
    
	
	/**
	 * Merge new db names with existing groups.
	 * 
	 * @param makePrimary
	 * @param names
	 */
	public void addDb(boolean makePrimary, final List<String> theDbAndSynonyms) {
		String firstName = dbName(theDbAndSynonyms.get(0));
		
		// find a group and merge
		boolean isNewGroup = true;
		for(List<String> g : this.synonyms) {
			if(!Collections.disjoint(g, theDbAndSynonyms)) {
				isNewGroup = false;
				for(String n : theDbAndSynonyms) {
					if(!g.contains(n)) {
						g.add(n);
					}
				}
				if(makePrimary) {
					if(g.contains(firstName)) {
						g.remove(firstName);
					}
					g.add(0, firstName);
				}
				break;
			}
		}
		
		// when merged with existing group
		if(!isNewGroup) {
			// make sure groups do not intersect
			assert(xcheck());
		} else {
			// add as a new group
			this.synonyms.add(theDbAndSynonyms);
		}

	}

	
	private boolean xcheck() {
		Object[] lists = this.synonyms.toArray();
		for(int i=0; i < lists.length; i++) {
			List<String> li = (List<String>) lists[i];
			for(int j=i+1; j < lists.length; j++) {
				if(!Collections.disjoint(li, (Collection<?>) lists[j])) {
					log.error("different synonyms groups intercection found: " 
						+ li.toString() + " has names in common with " 
						+ lists[j].toString());
					return false;
				}
			}
		}
		return true; // now intersection found :)
	}
	
	/**
	 * Removes tail spaces and converts to upper case
	 * 
	 * @param name
	 * @return
	 */
	public String dbName(String name) {
		return name.trim().toUpperCase();
	}
	
	
    /**
     * Gets database name and its variants.
     * The first in the list is the recommended one.
     *
     * @param name
     * @return set of equivalent database names
     */
    public List<String> getSynonymsForDbName(String name) {
		String dbName = dbName(name);
		
		for(List<String> group : synonyms) {
			if (group.contains(dbName)) {
				return group;
			}
		}
		return new ArrayList<String>();
    }

    
    /**
     * Gets the primary name for the DB.
     * It returns NULL for "unknown" database name. 
     * 
     * @param name
     * @return
     */
    public String getPrimaryDbName(String name) {
    	List<String> syns = getSynonymsForDbName(name);
    	return (syns.isEmpty()) ? null : syns.get(0);
    }
    
    /**
     * Checks whether the ID format is valid for the database.
     * 
     * @param db
     * @param id
     * @return 'false' if it fails, 'true' otherwise or when there are no patterns 
     *         available to check.
     */
    public boolean checkIdFormat(String db, String id) {
    	String dbName = dbName(db);
       	return dataPatterns.get(dbName).matcher(id).find();
    }

    /**
     * @param name a database name (used in xrefs)
     * @return true if it's possible to check the format.
     */
    public boolean canCheckIdFormatIn(String name) {
        String db = dbName(name);
        return dataPatterns.get(db) != null;
    }


    /**
     * Gets the regular expression corresponding 
     * to the database.
     * 
     * @param db a database name
     * @return regular expression to check its ID
     */
    public String getRegexpString(String db) {
    	return dataPatterns.get(dbName(db)).pattern();
    }

    
    public boolean isSynonyms(String db1, String db2) {
    	// dbName function is used here also to get right 
    	// name capitalization
    	return getSynonymsForDbName(db1).contains(dbName(db2));
    }
    
}


