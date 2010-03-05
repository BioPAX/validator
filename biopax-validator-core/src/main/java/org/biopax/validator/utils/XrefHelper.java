package org.biopax.validator.utils;

import java.util.*;
import java.util.regex.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import psidev.psi.tools.ontology_manager.OntologyUtils;
import psidev.psi.tools.ontology_manager.impl.OntologyTermImpl;
import psidev.psi.tools.ontology_manager.interfaces.OntologyAccess;

import uk.ac.ebi.miriam.lib.MiriamLink;

/**
 * This helps validate Xrefs, trying different database synonymous 
 * and misspelled names, and validating referenced 'id' against the 
 * MIRIAM database.
 *
 * @author rodche
 */
public class XrefHelper extends MiriamLink {
    private static final Log log = LogFactory.getLog(XrefHelper.class);
    
    private Map<String, Pattern> dataPatterns;
    private Set<List<String>> synonyms;
    private OntologyManagerAdapter ontologyManager;
	
    
    public XrefHelper(Set<List<String>> extraSynonyms, OntologyManagerAdapter ontologyManager) 
    		throws Exception 
    {   	
    	this.ontologyManager = ontologyManager;
    	
		if(!isLibraryUpdated() && log.isInfoEnabled()) {
			log.info("There is a new version of the MiriamLink available!");
		}
		
		// all database names and ID patterns go here
		this.dataPatterns = new HashMap<String, Pattern>();
		
		// add pre-configured synonyms, if any
		this.synonyms = (extraSynonyms != null && !extraSynonyms.isEmpty()) 
			? this.synonyms = extraSynonyms 
			: new HashSet<List<String>>();
		
		// adds names and assigns regexps from Miriam;
		// also makes those names primary synonyms
		for (String dt : getDataTypesName()) {
			String db = dbName(dt);
			String regexp = getDataTypePattern(dt);
			Pattern pattern = Pattern.compile(regexp);
			List<String> synonyms = new ArrayList<String>();
			synonyms.add(db);
			String[] otherNames = getDataTypeSynonyms(dt);
			if (otherNames != null && otherNames.length>0) {
				synonyms.addAll(Arrays.asList(otherNames));
			}
			addDb(true, synonyms);
			// set patterns for all synonyms
			for (String s : getSynonymsForDbName(db)) {
				dataPatterns.put(s, pattern);
			}
		}

		// load db names from MI 'database citation'
		OntologyAccess mi = ontologyManager.getOntologyAccess("MI");
		Collection<String> termNames = OntologyUtils.getTermNames(
				mi.getAllChildren(new OntologyTermImpl("MI:0444"))); 
		for (String term : termNames) {
			String db = dbName(term);
			
		}
		
    }

	
	/**
	 * Merge new db names with existing groups.
	 * 
	 * @param makePrimary
	 * @param names
	 */
	public void addDb(boolean makePrimary, final List<String> synonyms) {
		String firstName = dbName(synonyms.get(0));
		
		// find a group and merge
		boolean isNewGroup = true;
		for(List<String> g : this.synonyms) {
			if(!Collections.disjoint(g, synonyms)) {
				isNewGroup = false;
				for(String n : synonyms) {
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
			this.synonyms.add(synonyms);
		}

	}

	
	private boolean xcheck() {
		Object[] lists = this.synonyms.toArray();
		for(int i=0; i < lists.length; i++) {
			List<String> li = (List<String>) lists[i];
			for(int j=i+1; j < lists.length; j++) {
				if(!Collections.disjoint(li, (Collection<?>) lists[j])) {
					return false; // they intersect :(
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


