package org.biopax.psidev.ontology_manager;


import org.biopax.psidev.ontology_manager.impl.OntologyLoaderException;

import java.util.*;

public interface OntologyManager {

  void putOntology(String ontologyID, OntologyAccess ontologyAccess);

  Set<String> getOntologyIDs();


  OntologyAccess getOntology(String ontologyID);


  boolean containsOntology(String ontologyID);


  void loadOntologies(Properties cfg)
    throws OntologyLoaderException;

  /**
   * Search for terms using a preferred name or synonym.
   * The search is case insensitive.
   * It iterates through all loaded ontologies, so use with caution!
   *
   * @param name - term name (not ID)
   * @return
   */
  Set<OntologyTermI> searchTermByName(String name);

  /**
   * Search for terms by name or synonym.
   * The search is case insensitive.
   * It still iterates over all available ontologies, but
   * skips ones other than specified in the second parameter.
   *
   * @param name - term name (not ID)
   * @param ontologies to look into
   * @return terms
   */
  Set<OntologyTermI> searchTermByName(String name, Set<String> ontologies);


  /**
   * Finds an ontology term by its accession.
   *
   * @param acc
   * @return term
   */
  OntologyTermI findTermByAccession(String acc);

  /**
   * Finds a term by URI or URN.
   * @param uri Miriam URN or Identifiers.org URI of the ontology term.
   * @return term
   */
  OntologyTermI getTermByUri(String uri);

  Set<OntologyTermI> getDirectChildren(String urn);

  Set<OntologyTermI> getDirectParents(String urn);

  Set<OntologyTermI> getAllChildren(String urn);

  Set<OntologyTermI> getAllParents(String urn);

  boolean isChild(String parentUrn, String urn);

  OntologyTermI findTerm(OntologyAccess ontologyAccess, String term);


    /**
     * Collect all available names in the given collection of OntologyAccess terms.
     * @param terms the terms for which we want the names.
     * @return a non null collection of names.
     */
  //TODO: if min. java is 10, we'd replace 'static' with 'default'
  static Collection<String> getTermNames(Collection<OntologyTermI> terms) {
    if ( terms == null ) {
      return Collections.emptyList();
    }
    Collection<String> names = new ArrayList<>( terms.size() );
    for ( OntologyTermI term : terms ) {
      names.add( term.getPreferredName() );
      names.addAll( term.getNameSynonyms() );
    }
    return names;
  }

}