package org.biopax.validator.utils;

import java.util.*;
import java.util.regex.*;
import javax.annotation.PostConstruct;
import javax.xml.transform.stream.StreamSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
    
    private Map<String, Regexp> databases;
    private DbSynonyms dbSynonyms;
    private Resource miriamXmlResource;
    private Unmarshaller miriamUnmarshaller;
    private OntologyUtils ontologyUtils;
        
    public XrefHelper(DbSynonyms synonyms, Resource miriamXmlResource, 
    		Unmarshaller miriamUnmarshaller, OntologyUtils ontologyUtils) {
    	this.dbSynonyms = synonyms;
    	this.databases = new HashMap<String, Regexp>();
    	this.miriamXmlResource = miriamXmlResource;
    	this.miriamUnmarshaller = miriamUnmarshaller;
    	this.ontologyUtils = ontologyUtils;
    }
           
    @PostConstruct
    public void init() {
    	// adds user-configured synonyms to databases
    	for(List<String> group : dbSynonyms.getGroups()) {
    		for(String db : group) {
    			databases.put(db, null); 
    		}
    	}
    	
    	// adds names from MI
    	loadMIDatabaseCitation();
    	
    	
    	// adds names and assigns regexps from Miriam;
    	// also makes those names primary synonyms
    	importMiriam();
        
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
       

    private void loadMIDatabaseCitation() {
        //OntologyAccess os = ontologyUtils.getOntologyAccess("MI");
        Collection<String> terms = new HashSet<String>(); //= OntologyUtils.getTermNames(os.getValidTerms("MI:0444",true,false));
        for(String term: terms) {
            String db = dbName(term);
            databases.put(db, null);
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
        	return getRegexp(dbName).find(id);
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

    public void importMiriam() {
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
                Regexp rx = new Regexp();
                rx.setExpr(dt.getPattern()); // this also compiles the startChar
                dbSynonyms.addSynonym(db, db, true); // make it a primary db name
                if (dt.getSynonyms() != null) {
                    dbSynonyms.addSynonyms(dt.getSynonyms().getSynonym(), db);
                }
                // set patterns for all synonyms
                for (String s : getSynonymsForDbName(db)) {
                	databases.put(s, rx);
                }
            }   
        } catch (Exception ex) {
            throw new RuntimeException("Error in MIRIAM import.", ex);
        }
    }
    

    /**
     * To create ID validating patterns (compiled).
     */
    private final class Regexp {
        private String expr;
        private Pattern pattern;

        public void setExpr(String re) {
            this.expr = re;
            pattern = Pattern.compile(expr);
        }

        public String getExpr() {
            return expr;
        }

        public boolean find(String id) {
            Matcher m = pattern.matcher(id);
            return m.find();
        }
        
        @Override
        public String toString() {
        	return expr;
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
    	return databases.get(dbName(db)).getExpr();
    }

    private Regexp getRegexp(String db) {
    	return databases.get(dbName(db));
    }
    
    public String dbName(String name) {
    	return dbSynonyms.dbName(name);
    }
    
    public boolean hasSynonyms(String name) {
    	return !getSynonymsForDbName(name).isEmpty();
    }
    
    public boolean areSynonyms(String db1, String db2) {
    	// dbName function is also to use right (pre-configured) capitalization
    	return getSynonymsForDbName(db1).contains(dbName(db2));
    }
}


