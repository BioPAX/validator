package org.biopax.psidev.ontology_manager;

/*
 * #%L
 * Ontologies Access
 * %%
 * Copyright (C) 2008 - 2013 University of Toronto (baderlab.org) and Memorial Sloan-Kettering Cancer Center (cbio.mskcc.org)
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
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