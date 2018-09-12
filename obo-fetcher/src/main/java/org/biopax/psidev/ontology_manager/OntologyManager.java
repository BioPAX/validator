package org.biopax.psidev.ontology_manager;

/*
 *
 */

import java.util.Properties;
import java.util.Set;

import org.biopax.psidev.ontology_manager.impl.OntologyLoaderException;


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
	 * @return
	 */
	Set<OntologyTermI> searchTermByName(String name, Set<String> ontologies);
	
	
	/**
	 * Finds an ontology term by its accession.
	 * 
	 * @param acc
	 * @return
	 */
	OntologyTermI findTermByAccession(String acc);
}