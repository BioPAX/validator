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

import com.opensymphony.oscache.base.NeedsRefreshException;

/**
 * This helps validate Xrefs, trying different database synonymous 
 * and misspelled names, and validating referenced 'id' against the 
 * MIRIAM database.
 *
 * @author rodche
 */
public class XrefHelper {
    private static final Log log = LogFactory.getLog(XrefHelper.class);
    private static final String CACHE_KEY = "xrefHelper";
    
    private Map<String, Pattern> databases;
    private DbSynonyms dbSynonyms;
    private Resource miriamXmlResource;
    private Unmarshaller miriamUnmarshaller;
    private OntologyUtils ontologyUtils;
        
    public XrefHelper(DbSynonyms synonyms, Resource miriamXmlResource, 
    		Unmarshaller miriamUnmarshaller, OntologyUtils ontologyUtils) {
    	this.dbSynonyms = synonyms;
    	this.databases = new HashMap<String, Pattern>();
    	this.miriamXmlResource = miriamXmlResource;
    	this.miriamUnmarshaller = miriamUnmarshaller;
    	this.ontologyUtils = ontologyUtils;
    }
           
    @SuppressWarnings("unchecked")
	@PostConstruct
    public void init() {
    	// first, try getting "databases" from cache
    	try {
			databases = (Map<String, Pattern>) ontologyUtils
				.getCacheAdministrator().getFromCache(CACHE_KEY);
			return;
		} catch (NeedsRefreshException e) {
			if(log.isDebugEnabled())
				log.debug("Re-bulding the db name/pattern cache...");
		}
    	
		// Retrieve database names and ID patterns
		
    	// adds user-configured synonyms to databases
    	for(List<String> group : dbSynonyms.getGroups()) {
    		for(String db : group) {
    			databases.put(db, null); 
    		}
    	}
    	
		// loads names from MI: all children terms of 'database citation'
		Set<String> terms = ontologyUtils.getTermNames(new CvTermRestriction(
				"MI:0444", "MI", false, UseChildTerms.ALL, false));
		for (String term : terms) {
			String db = dbName(term);
			databases.put(db, null);
		}

    	// adds names and assigns regexps from Miriam;
    	// also makes those names primary synonyms
    	importMiriam();
        
    	
    	// add to chache
    	boolean saved = false;
    	try {
    		ontologyUtils.getCacheAdministrator().putInCache(CACHE_KEY, databases);
    		ontologyUtils.getCacheAdministrator().flushEntry(CACHE_KEY);
    		saved = true;
    	} finally {
    		if(!saved) {
    			ontologyUtils.getCacheAdministrator().cancelUpdate(CACHE_KEY);
    		}
    	}
    	
        if(log.isTraceEnabled()) {
        	StringBuffer sb = new StringBuffer("(test) GO synonyms loaded:");
        	for(String sy: dbSynonyms.getSynonyms("go")) {
        		sb.append(sy).append(';');
        	}
        	log.trace(sb.toString());
        	
        	sb = new StringBuffer("Allowed DB names and ID-check patterns:");
        	String[] keys = databases.keySet().toArray(new String[]{});  
            Arrays.sort(keys);
        	for(String db: keys) {
        		sb.append(db).append(" : ").append(databases.get(db)).append('\n');
        	}
        	log.trace(sb.toString());
        }
    }
       
    /**
     * Lists database name variants.
     *
     * @param name
     * @return set of equivalent database names
     */
    public List<String> getSynonymsForDbName(String name) {
       	return dbSynonyms.getSynonyms(name);
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

    // get db/regex form MIRIAM (xml export)
    private void importMiriam() {
        try {
            Miriam miriam = (Miriam) miriamUnmarshaller.unmarshal(
            		new StreamSource(miriamXmlResource.getInputStream()));
            
            if (log.isDebugEnabled()) {
                log.debug("MIRIAM XML imported, version: "
                    + miriam.getDataVersion() + ", datatypes: "
                    + miriam.getDatatype().size());
            }
            
            for(Miriam.Datatype dt: miriam.getDatatype()) {
                String db = dbName(dt.getName());            
                Pattern pattern = Pattern.compile(dt.getPattern());
                dbSynonyms.addSynonym(db, db, true); // make it a primary db name
                if (dt.getSynonyms() != null) {
                    dbSynonyms.addSynonyms(dt.getSynonyms().getSynonym(), db);
                }
                // set patterns for all synonyms
                for (String s : getSynonymsForDbName(db)) {
                	databases.put(s, pattern);
                }
            }   
        } catch (Exception ex) {
            throw new RuntimeException("Error in MIRIAM import.", ex);
        }
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

    public String dbName(String name) {
    	return dbSynonyms.dbName(name);
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


