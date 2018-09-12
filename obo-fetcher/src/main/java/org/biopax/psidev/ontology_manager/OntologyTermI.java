package org.biopax.psidev.ontology_manager;

/*
 *
 */


import java.util.Collection;

/**
 * Author: Florian Reisinger
 * Date: 09-Jul-2008
 */
public interface OntologyTermI {

    String getTermAccession();

    String getPreferredName();

    void setTermAccession( String accession );

    void setPreferredName( String preferredName );

    Collection<String> getNameSynonyms();

    void setNameSynonyms( Collection<String> nameSynonyms );
    
    String getOntologyId();
    
    void setOntologyId(String ontologyId);
    
}
