package uk.ac.ebi.ols;


import java.io.IOException;


/**
 * Constants for the loader package and all loaded ontolgies#
 *
 * @author Richard Cote
 * @version $Id: Loader.java,v 1.7 2006/03/28 16:31:41 rglcote Exp $
 */
public interface Loader {

    public static final String RELATION_TYPE = "relation_type";
    public static final String SYNONYM_TYPE = "synonym_type";

    public Ontology getOntology() throws IOException;

}
