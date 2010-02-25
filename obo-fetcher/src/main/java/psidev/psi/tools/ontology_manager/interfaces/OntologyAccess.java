package psidev.psi.tools.ontology_manager.interfaces;

import psidev.psi.tools.ontology_manager.impl.local.OntologyLoaderException;

import java.io.File;
import java.net.URI;
import java.util.Set;

/**
 * Defines what can be asked to an ontology.
 *
 * @author Florian Reisinger
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @since 2.0.0
 */
public interface OntologyAccess {

    /**
     * Load ontology data.
     *
     * @param ontologyID the identifier of this ontology.
     * @param name       the name of the ontology
     * @param version    the version of the ontology
     * @param format     the format of the ontology
     * @param uri        the URI of the ontology
     * @throws OntologyLoaderException
     */
    void loadOntology( String ontologyID, String name, String version, String format, URI uri ) throws OntologyLoaderException;

    /**
     * For the OntologyAccess implementation that supports it, sets a directory that can be used as cache for
     * downloaded ontology files.
     *
     * @param directory location of the cache.
     */
    void setOntologyDirectory( File directory );

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

    /**
     * Is the given ontology term obsolete.
     *
     * @param term the term of interrest.
     * @return true of the term is obsolete, false otherwise.
     */
    public boolean isObsolete( OntologyTermI term );

    /**
     * Provides the direct parents of the given terms in a non null set.
     *
     * @param term the term for which we want the direct parents.
     * @return a non null set of ontology terms.
     */
    public Set<OntologyTermI> getDirectParents( OntologyTermI term );

    /**
     * Provides the direct children of the given terms in a non null set.
     *
     * @param term the term for which we want the direct children.
     * @return a non null set of ontology terms.
     */
    public Set<OntologyTermI> getDirectChildren( OntologyTermI term );

    /**
     * Provides all parents of the given terms in a non null set.
     *
     * @param term the term for which we want all parents.
     * @return a non null set of ontology terms.
     */
    public Set<OntologyTermI> getAllParents( OntologyTermI term );

    /**
     * Provides all children of the given terms in a non null set.
     *
     * @param term the term for which we want all children.
     * @return a non null set of ontology terms.
     */
    public Set<OntologyTermI> getAllChildren( OntologyTermI term );
}