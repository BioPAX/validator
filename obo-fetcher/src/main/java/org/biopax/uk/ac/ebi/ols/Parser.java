package org.biopax.uk.ac.ebi.ols;

import org.obo.datamodel.LinkedObject;
import org.obo.datamodel.OBOObject;
import org.obo.datamodel.OBOSession;

import java.util.HashMap;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: rcote
 * Date: 22-May-2009
 * Time: 10:45:20
 */
public interface Parser {

    public enum PARSER_TYPE {
        OBO_PARSER, OWL_PARSER
    }

    Set<OBOObject> getTerms();

    HashMap<String, Integer> computeChildPaths(int distance, Set relationshipType, LinkedObject term);

    OBOSession getSession();

    Set<OBOObject> getRootTerms(boolean useGreedy);

}
