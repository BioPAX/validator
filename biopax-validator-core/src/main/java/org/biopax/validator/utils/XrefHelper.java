package org.biopax.validator.utils;

import java.util.*;
import java.util.regex.*;
import javax.annotation.PostConstruct;
import javax.xml.transform.stream.StreamSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.validator.impl.CvTermRestriction;
import org.biopax.validator.impl.CvTermRestriction.UseChildTerms;

import net.biomodels.miriam.Miriam;
import org.springframework.core.io.Resource;
import org.springframework.oxm.Unmarshaller;

/**
 * This helps validate Xrefs, trying different database synonymous 
 * and misspelled names, and validating referenced 'id' against the 
 * MIRIAM database.
 *
 * @author rodche
 */
public class XrefHelper {
    private static final Log log = LogFactory.getLog(XrefHelper.class);
    
    private Map<String, Pattern> databases;
    private OntologyManagerAdapter ontologyManager;
    private Miriam miriam;
	private Set<List<String>> customDbSynonyms;
    
    public XrefHelper(Set<List<String>> customDbSynonyms, Resource miriamXmlResource, 
    		Unmarshaller miriamUnmarshaller, OntologyManagerAdapter ontologyManager) 
    	throws Exception 
    {
    	this.customDbSynonyms = (customDbSynonyms != null) 
    		? customDbSynonyms 
    			: new HashSet<List<String>>();
    	
    	this.ontologyManager = ontologyManager;
    	
    	// load Miriam
    	this.miriam = (Miriam) miriamUnmarshaller.unmarshal(
        		new StreamSource(miriamXmlResource.getInputStream()));
        if (log.isDebugEnabled()) {
            log.debug("MIRIAM XML imported, version: "
                + miriam.getDataVersion() + ", datatypes: "
                + miriam.getDatatype().size());
        }
    }
           
    @SuppressWarnings("unchecked")
	@PostConstruct
	public void init() {
		// Retrieve database names and ID patterns
		databases = new HashMap<String, Pattern>();

		// adds user-configured synonyms to databases
		for (List<String> group : customDbSynonyms) {
			for (String db : group) {
				databases.put(db, null);
			}
		}

		// loads names from MI: all children terms of 'database citation'
		Set<String> terms = ontologyManager.getTermNames(new CvTermRestriction(
				"MI:0444", "MI", false, UseChildTerms.ALL, false));
		for (String term : terms) {
			String db = dbName(term);
			databases.put(db, null);
		}

		// adds names and assigns regexps from Miriam;
		// also makes those names primary synonyms
		importMiriam();
	}
    
    // get db names and regex form MIRIAM
	private void importMiriam() {
		for (Miriam.Datatype dt : miriam.getDatatype()) {
			String db = dbName(dt.getName());
			Pattern pattern = Pattern.compile(dt.getPattern());
			addSynonym(db, db, true); // make it a primary db name
			if (dt.getSynonyms() != null) {
				addSynonyms(dt.getSynonyms().getSynonym(), db);
			}
			// set patterns for all synonyms
			for (String s : getSynonymsForDbName(db)) {
				databases.put(s, pattern);
			}
		}
	}
       
	/**
	 * Gets user-defined synonymous groups of database names
	 * (order matters within a group - the first is primary one)
	 * @return
	 */
	public Set<List<String>> getCustomDbSynonyms() {
		return customDbSynonyms;
	}
	
    /**
     * Gets Miriam data model.
     * 
     * @return
     */
    public Miriam getMiriam() {
		return miriam;
	}
	
	/**
	 * Adds alternative database name. 
	 * 
	 * @param newName
	 * @param name
	 * @param isToBePrimary
	 */
	public void addSynonym(final String newName, final String name, boolean isToBePrimary) {
		String synonym = dbName(newName);
		String member = dbName(name);
		List<String> list = getSynonymsForDbName(member);
		int idx = (isToBePrimary) ? 0 : list.size();
		if(list.isEmpty()) {
			list.add(member);
			customDbSynonyms.add(list);
			list.add(idx, synonym);
		} else {
			boolean alreadyPresent = list.contains(synonym);
			if(alreadyPresent && isToBePrimary) { 	
				list.remove(synonym);
			}
			list.add(idx, synonym);
		}
	}
	
	public void addSynonyms(Collection<String> synonyms, String member) {
		String dbName = dbName(member);
		List<String> g = getSynonymsForDbName(dbName);
		if(g.isEmpty()) {
			g.add(dbName);
			customDbSynonyms.add(g);
		}
		
		for(String n : synonyms) {
			n = dbName(n);
			if(!g.contains(n)) {
				g.add(n);
			}
		}
	}

	
	public String dbName(String name) {
		return name.trim().toUpperCase();
	}
	
	
    /**
     * Lists database name variants.
     *
     * @param name
     * @return set of equivalent database names
     */
    public List<String> getSynonymsForDbName(String name) {
		String dbName = dbName(name);
		for(List<String> g : customDbSynonyms) {
			if (g.contains(dbName)) {
				return g;
			}
		}
		// return empty list
		return new ArrayList<String>();
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
        if(canCheckIdFormatIn(dbName)) {
        	return databases.get(dbName).matcher(id).find();
        }
        return true;
    }

    /**
     * @param name a database name (used in xrefs)
     * @return true if it's possible to check the format.
     */
    public boolean canCheckIdFormatIn(String name) {
        String db = dbName(name);
        return contains(db) && databases.get(db) != null;
    }

    public boolean contains(String name) {
        String db = dbName(name);
        return databases.containsKey(db);
    }

    /**
     * Gets the regular expression corresponding 
     * to the database.
     * 
     * @param db a database name
     * @return regular expression to check its ID
     */
    public String getRegexpString(String db) {
    	return databases.get(dbName(db)).pattern();
    }

    
    public boolean hasSynonyms(String name) {
    	return !getSynonymsForDbName(name).isEmpty();
    }
    
    public boolean areSynonyms(String db1, String db2) {
    	// dbName function is used here also to get right 
    	// name capitalization
    	return getSynonymsForDbName(db1).contains(dbName(db2));
    }
    
}


