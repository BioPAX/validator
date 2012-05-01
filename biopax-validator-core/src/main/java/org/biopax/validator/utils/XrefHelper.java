package org.biopax.validator.utils;

import java.util.*;
import java.util.regex.*;

import javax.annotation.PostConstruct;

import org.apache.commons.collections15.collection.CompositeCollection;
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
    private CompositeCollection<String> allSynonyms; //set in init()!
    private BiopaxOntologyManager ontologyManager;
    private final CompositeCollection<String> extraGroups; //set in Constructor
    private Set<String> unofficialDbNames; // to be generated	
    
    public XrefHelper(Set<List<String>> extraSynonyms, 
    	BiopaxOntologyManager ontologyManager) 
    		throws Exception 
    {   	
		// all database names and ID patterns go here
		this.dataPatterns = new HashMap<String, Pattern>();
		
		// copy/normalize provided extra synonyms
		this.extraGroups = new CompositeCollection<String>();
		if(extraSynonyms != null) {
			for(List<String> group : extraSynonyms) {
				Collection<String> newG = new ArrayList<String>();
				for(String s : group) 
					newG.add(dbName(s)); //trim, uppercase, add
				this.extraGroups.addComposited(newG);
			}
		}
			
		this.ontologyManager = ontologyManager;
    }

    /**
     * initialize
     */
    @PostConstruct
    void init() {
		this.allSynonyms =  new CompositeCollection<String>();
		this.unofficialDbNames = new HashSet<String>();	
    	
    	// need a temporaty, local "all synonyms" collection
    	// (hide the member one)
    	CompositeCollection<String> allSynonyms = new CompositeCollection<String>();
    	
    	// first, we simple prepare lists of db synonyms,
    	// from  Miriam, MI, and "extra" set (already done - constructor arg.)
		for (String dt : MiriamLink.getDataTypesName()) {
			String regexp = MiriamLink.getDataTypePattern(dt);
			
			Pattern pattern = null;
			try {
				pattern= Pattern.compile(regexp);
			} catch (PatternSyntaxException e) {
				log.error(
					"Pattern compilation failed for MIRIAM " +
					"db=" + dt + "; regexp=" + dt + "; " + e);
			}
			String db = dbName(dt); //uppercase
			List<String> synonyms = new ArrayList<String>();
			synonyms.add(db);
			String[] names = MiriamLink.getNames(dt);
			if (names != null)
				for(String name : names) {
					String s = dbName(name);
					if(!synonyms.contains(s))
						synonyms.add(s); 
				}
			//save
			allSynonyms.addComposited(synonyms);
			
			// also associate primary name with ID patterns
			dataPatterns.put(db, pattern); // will be used with all synonyms
		}
    	
		// load all names from MI 'database citation'
		Ontology mi = ontologyManager.getOntology("MI");
		Collection<OntologyTermI> terms = mi.getAllChildren(new OntologyTermImpl("MI:0444")); 
		for (OntologyTermI term : terms) {
			List<String> synonyms = new ArrayList<String>();
			synonyms.add(dbName(term.getPreferredName()));
			for(String name : term.getNameSynonyms()) {
				String s = dbName(name);
				if(!synonyms.contains(s))
					synonyms.add(s); 
			}
			allSynonyms.addComposited(synonyms);
		}
    	
    	// second, we populate unofficialDbNames by comparing 
    	// allSynonyms (found so far) vs. extraSynonyms (provided)
		for(String db : extraGroups) {
			if(!allSynonyms.contains(db))
				unofficialDbNames.add(db);
		}
    	
		allSynonyms.addComposited((Collection<? extends String>[]) extraGroups.getCollections().toArray());
		
    	// find synonyms groups that overlap,
		Cluster<Collection<String>> clus = new Cluster<Collection<String>>() {
			@Override
			public boolean match(Collection<String> a, Collection<String> b) {
				return !Collections.disjoint(a, b);
			}
		};

		// find all overlapping groups of names (can be single-group "clusters" as well))
		// get the set of clusters (sets) of overlapping synonym lists! (i.e., we're in fact clustering List<String> objects)
		Set<Set<Collection<String>>> groupsOfGroups = clus.cluster(allSynonyms.getCollections(), Integer.MAX_VALUE);
		
		// perform overlapping groups merging
		// so that Miriam's preferred name goes first 
		for(Set<Collection<String>> groupsToMerge : groupsOfGroups) {
			if (groupsToMerge.size() > 1) {
				List<String> merged = new ArrayList<String>();
				String primary = null;
				for (Collection<String> group : groupsToMerge) {
					for (String name : group) {
						if (dataPatterns.containsKey(name))
							primary = name;
						if (!merged.contains(name))
							merged.add(name);
					}
				}

				if (primary != null) {
					merged.remove(primary);
					merged.add(0, primary);
				}
				this.allSynonyms.addComposited(merged);
			} else {
				assert !groupsToMerge.isEmpty(); //one!
				//not overlapping (with other) single group
				this.allSynonyms.addComposited(groupsToMerge.iterator().next());
			}
		}
    	
		//sanity checks
		assert(!getSynonymsForDbName("EntrezGene").isEmpty());	
		assert(!getSynonymsForDbName("chebi").isEmpty());
		assert xcheck();		
    }
    

	private boolean xcheck() {
		Object[] lists = allSynonyms.getCollections().toArray();
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
     * @param name case insensitive
     * @param groups TODO
     * @return set of equivalent database names
     */
    public List<String> getSynonymsForDbName(String name) {
    	return getSynonymsForName(name, allSynonyms);
    }

    
    private List<String> getSynonymsForName(String name, CompositeCollection<String> groups) {
    	String dbName = dbName(name);
		
		for(Collection<String> group : groups.getCollections()) {
			if (group.contains(dbName)) {
				return (List<String>) group; //copy to protect
			}
		}
		return Collections.emptyList();
    }

    
    /**
     * Gets the primary name for the DB.
     * It returns NULL for "unknown" database name. 
     * 
     * @param name case insensitive
     * @return
     */
    public String getPrimaryDbName(String name) {
    	String dbName = dbName(name);
		
		for(Collection<String> group : allSynonyms.getCollections()) {
			if (group.contains(dbName)) {
				return group.iterator().next(); //get the first
			}
		}
		
		return null;
    }
    
    
    /**
     * Checks whether the ID format is valid for the database.
     * Always use {@link #canCheckIdFormatIn(String)} before this method,
     * because it may throw an exception if you do not.
     * 
     * @param db case insensitive
     * @param id
     * @return 'false' if matcher fails, 'true' otherwise
     * @throws NullPointerException when no pattern available
     */
    public boolean checkIdFormat(String db, String id) {
    	String name = getPrimaryDbName(db);
    	Pattern p = dataPatterns.get(name);
       	return p.matcher(id).find();
    }

    /**
     * @param name a database name (used in xrefs), case insensitive
     * @return true if it's possible to check the format.
     */
    public boolean canCheckIdFormatIn(String name) {
        String db = getPrimaryDbName(name);
        return (db == null) ? false : dataPatterns.get(db) != null;
    }


    /**
     * Gets the regular expression corresponding 
     * to the database.
     * 
     * @param db a database name, case insensitive
     * @return regular expression to check its ID
     */
    public String getRegexpString(String db) {
    	String name = getPrimaryDbName(db);
    	Pattern p = dataPatterns.get(name);
    	return (p != null) ? p.pattern() : null;
    }

    
    //case insensitive
    public boolean isSynonyms(String db1, String db2) {
    	// dbName function is used here also to get right 
    	// name capitalization
    	return getSynonymsForDbName(db1).contains(dbName(db2));
    }
    
    
    /**
     * Get the immutable list of 
     * DB misspellings and unofficial names,
     * which the Validator recognizes, reports 
     * warnings, and is able to replace with
     * corresponding official names; and which
     * otherwise, using MI or Miriam, 
     * cannot be resolved.
     * 
     * @return
     */
    Set<String> getUnofficialDbNames() {
		return unofficialDbNames;
	}
    
    /**
     * Checks whether the db name is known (configured) 
     * misspellings or unofficial name,
     * which the Validator can recognize, report 
     * warning, and replace with official names; and which
     * otherwise, using MI or Miriam, cannot be resolved.
     * 
     * @param db case insensitive
     * @return
     */
	public boolean isUnofficialOrMisspelledDbName(final String db) {
		return getUnofficialDbNames().contains(dbName(db));
	}

}


