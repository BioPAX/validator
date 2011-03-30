package psidev.ontology_manager;

import java.io.File;
import java.util.Map;
import java.util.Set;

import org.springframework.core.io.Resource;

import psidev.ontology_manager.impl.OntologyLoaderException;

public interface OntologyManager {

	Ontology putOntology(String ontologyID,
			Ontology ontology);


	Set<String> getOntologyIDs();


	Ontology getOntology(String ontologyID);


	void setOntologyDirectory(File ontologyDirectory);

	
	boolean containsOntology(String ontologyID);


	void loadOntologies(Map<String, Resource> cfg)
			throws OntologyLoaderException;

	/**
	 * Search for terms using a name (synonym) name.
	 * The search is case insensitive.
	 * It iterates through all loaded ontologies, so use with caution!
	 * 
	 * @param name - term name (not ID)
	 * @return
	 */
	Set<OntologyTermI> searchTermByName(String name);
	
	OntologyTermI findTermByAccession(String acc);
}