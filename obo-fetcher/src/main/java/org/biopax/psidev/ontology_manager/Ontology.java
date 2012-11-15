package org.biopax.psidev.ontology_manager;

import java.util.Collection;
import java.util.Set;

/**
 * Defines what can be asked to an ontology.
 *
 * @author Florian Reisinger
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @author rodche (baberlab.org) - re-factoring
 * @since 2.0.0
 */
public interface Ontology {

    /**
     * This method builds a set of all allowed terms based on the specified parameters.
     *
     * @param accession     the accession number of the term wanted.
     * @param allowChildren whether child terms are allowed.
     * @param useTerm       whether to include the specified term ID in the set of allowed IDs.
     * @return a non null set of allowed ontology terms for the specified parameters.
     */
    public Set<OntologyTermI> getValidTerms( String accession, boolean allowChildren, boolean useTerm );

    /**
     * Search a term by accession number.
     *
     * @param accession the accession to be searched for.
     * @return an ontology term or null if not found.
     */
    public OntologyTermI getTermForAccession( String accession );
    
    void setName(String name);
       
    String getName();
    
    boolean hasTerms();

    OntologyTermI search( String id );

    Collection<OntologyTermI> getRoots();

    Collection<OntologyTermI> getOntologyTerms();

    Collection<OntologyTermI> getObsoleteTerms();

    boolean isObsolete( OntologyTermI term );

    Set<OntologyTermI> getDirectParents( OntologyTermI term );

    Set<OntologyTermI> getDirectChildren( OntologyTermI term );

    Set<OntologyTermI> getAllParents( OntologyTermI term );

    Set<OntologyTermI> getAllChildren( OntologyTermI term );
    
    void addLink( String parentId, String childId );
    
    void addTerm( OntologyTermI term );
    
    void addObsoleteTerm( OntologyTermI term );

}