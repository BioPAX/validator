package org.biopax.ols.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.ols.Parser;
import org.obo.dataadapter.OBOAdapter;
import org.obo.dataadapter.OBOFileAdapter;
import org.obo.datamodel.*;
import org.obo.util.TermUtil;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Abstract base class to interact with OBOEdit codebase and parse OBO files
 *
 * @author Richard Cote
 * @version $Id: AbstractParser.java,v 1.9 2008/04/16 13:49:44 rglcote Exp $
 */
public class OBO2FormatParser implements Parser {
    private static Log logger = LogFactory.getLog(OBO2FormatParser.class);

    private OBOSession session;

    public OBO2FormatParser(String filePath) throws Exception {
        OBOFileAdapter adapter = new OBOFileAdapter();
        OBOFileAdapter.OBOAdapterConfiguration config = new OBOFileAdapter.OBOAdapterConfiguration();
        config.getReadPaths().add(filePath);
        session = adapter.doOperation(OBOAdapter.READ_ONTOLOGY, config, null);
    }

    public OBO2FormatParser(Collection<String> filePaths) throws Exception {
        OBOFileAdapter adapter = new OBOFileAdapter();
        OBOFileAdapter.OBOAdapterConfiguration config = new OBOFileAdapter.OBOAdapterConfiguration();
        config.getReadPaths().addAll(filePaths);
        session = adapter.doOperation(OBOAdapter.READ_ONTOLOGY, config, null);
    }

    /**
     * returns all the terms parsed from an ontology
     *
     * @return a set of OBOClass/Instance objects, or null if session is not initialized
     */
    public Set<OBOObject> getTerms() {
        LinkDatabase lnkDb = session.getLinkDatabase();
        HashSet<OBOObject> terms = new HashSet<>();
        for (IdentifiedObject io : lnkDb.getObjects()) {
            //do not return built-in obo: constructs
            if ((io instanceof OBOClass || io instanceof Instance) 
            		&& !io.getID().toLowerCase().startsWith("obo:")) {
                terms.add((OBOObject) io);
            }
        }
        return terms;
    }

    /**
     * returns the OBOSession of the underlying parser
     *
     * @return the OBOSession object
     */
    public OBOSession getSession() {
        return session;
    }

    /**
     * returns the root terms of an ontology
     *
     * @return a collection of OBOClass terms
     * @throws IllegalStateException if the session is not initialized
     */
    public Set<OBOObject> getRootTerms(boolean useGreedy) {

        HashSet<OBOObject> roots = new HashSet<>();
        /*
	     * {@link RootAlgorithm#GREEDY GREEDY} root algorithm.
     	 *
     	 * @param linkDatabase
     	 *            the linkDatabase to check
     	 * @param includeTerms
     	 *            whether to include root terms
     	 * @param includeProperties
     	 *            whether to include root properties
     	 * @param includeObsoletes
     	 *            whether to include obsolete terms & properties
     	 * @param includeInstances
     	 *            whether to include instances
         */

        if (useGreedy) {
            //use greedy root detection
            // returns all non-obsolete root terms in a given LinkDatabase
            Collection<OBOClass> tmpRoots = TermUtil.getRoots(session);
            roots.addAll(tmpRoots);
        } else {
            //use strict root detection
            Collection<LinkedObject> tmpRoots = TermUtil.getRoots(RootAlgorithm.STRICT, session.getLinkDatabase());
            for (LinkedObject lnk : tmpRoots) {
                if (lnk instanceof OBOClass) {
                    roots.add((OBOClass) lnk);
                }
            }
        }

        return roots;
    }


    /**
     * Will compute all the term paths from a given term to all its children, irrespective of distance
     *
     * @param distance         Distance in arcs between nodes. Set to 1 to begin, will be incremented in recursive method calls
     * @param term             The term to scan for a given distance
     * @param relationshipType - a set of possible synonyms for the type of relationship to link the terms (is_a, part_of, develops_from)
     * @return A hashmap where the key is a term ID and the value is an Integet repersenting the distance
     */
    private HashMap<String, Integer> computeChildPaths(int distance, Set relationshipType, LinkedObject term,
                                                       HashMap<String, Integer> paths)
    {
        //get all children
        Collection<Link> children = term.getChildren();

        //iterate over children
        for (Link trm : children) {
            //add relationship
            if (relationshipType.contains(trm.getType().getID())) {
                paths.put(trm.getChild().getID(), distance);
                //we're building up the map here, so we don't care about the return
                try {
                    computeChildPaths(distance + 1, relationshipType, trm.getChild(), paths);
                } catch (StackOverflowError e) {
                    logger.error("Stack overflow when computing child paths for: " + term.getID() + " for relationships: " + relationshipType);
                    throw new IllegalStateException(e);
                }
            }
        }

        return paths;
    }

    /**
     * Will compute all the term paths from a given term to all its children, irrespective of distance
     *
     * @param distance         Distance in arcs between nodes. Set to 1 to begin, will be incremented in recursive method calls
     * @param relationshipType - a set of possible synonyms for the type of relationship to link the terms (is_a, part_of, develops_from)
     * @param term             The term to scan for a given distance
     * @return A hashmap where the key is a term ID and the value is an Integet repersenting the distance
     */
    public HashMap<String, Integer> computeChildPaths(int distance, Set relationshipType, LinkedObject term) {
        //the true call is required only once, at each call
        return computeChildPaths(distance, relationshipType, term, new HashMap<>());
    }

}