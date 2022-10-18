package org.biopax.ols;

import java.io.IOException;

/**
 * Constants for the loader package and all loaded ontolgies#
 *
 * @author Richard Cote
 * @version $Id: Loader.java,v 1.7 2006/03/28 16:31:41 rglcote Exp $
 */
public interface Loader {
    String RELATION_TYPE = "relation_type";
    String SYNONYM_TYPE = "synonym_type";

    Ontology getOntology() throws IOException;
}
