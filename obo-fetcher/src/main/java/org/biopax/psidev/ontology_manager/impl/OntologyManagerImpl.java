package org.biopax.psidev.ontology_manager.impl;

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


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.psidev.ontology_manager.OntologyAccess;
import org.biopax.psidev.ontology_manager.OntologyManager;
import org.biopax.psidev.ontology_manager.OntologyTermI;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

import java.net.*;
import java.util.*;

/**
 * Central access to configured OntologyAccess.
 *
 * @author Florian Reisinger
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @author rodche (baderlab.org) - re-factored for the BioPAX Validator
 * @since 2.0.0
 */
public class OntologyManagerImpl implements OntologyManager {

    public static final Log log = LogFactory.getLog( OntologyManagerImpl.class );
   
    private static final ResourceLoader LOADER = new DefaultResourceLoader();
    
    /**
     * The Map that holds the Ontologies.
     * The key is the ontology ID and the value is a ontology inplementing the OntologyAccess interface.
     */
    private Map<String, OntologyAccess> ontologyMap;

    /**
     * Create a new OntologyManagerImpl with no configuration (no associated ontology).
     */
    public OntologyManagerImpl() {
    	ontologyMap = new HashMap<String, OntologyAccess>();
    }

    /** 
     * Creates a new OntologyManagerImpl managing the ontology specified in the config map.
     *
     * @param cfg configuration properties for the manager (ID=resource_location).
     * @throws OntologyLoaderException if the config file could not be parsed or the loading of a ontology failed.
     */
	public OntologyManagerImpl(Properties cfg)
			throws OntologyLoaderException 
	{
		this();
		loadOntologies(cfg);
		log.debug("Successfully created and configured new OntologyManagerImpl.");
	}


    public void putOntology( String ontologyID, OntologyAccess ontologyAccess ) {
        if ( ontologyMap.containsKey( ontologyID ) ) {
            log.warn( "OntologyAccess with the ID '" + ontologyID + "' already exists. Overwriting!" );
        }
        ontologyMap.put( ontologyID, ontologyAccess );
    }


    public Set<String> getOntologyIDs() {
        return ontologyMap.keySet();
    }


    public OntologyAccess getOntology( String ontologyID ) {
        return ontologyMap.get( ontologyID );
    }


    public boolean containsOntology( String ontologyID ) {
        return ontologyMap.containsKey( ontologyID );
    }

    
    public void loadOntologies( Properties config ) 
    	throws OntologyLoaderException 
    {
        if ( config != null && !config.isEmpty()) {
            for ( Object ontId : config.keySet() ) 
            {
            	String key = (String) ontId;
            	try {
                	URI uri = LOADER.getResource(config.getProperty(key)).getURI();
               		log.info( "Loading ontology: ID= " + ontId + ", uri=" + uri);
                    OntologyAccess oa = fetchOntology( key, "OBO", uri );
                    putOntology(key, oa);
                } catch ( Throwable e ) { //using Throwable because StackOverflowError is also possible here
                    throw new OntologyLoaderException("Failed loading/parsing ontology " + key 
                    	+ " from " + config.getProperty(key), e );
                }
            }
        } else {
        	throw new OntologyLoaderException("OntologyAccess configuration map is missing or empty (map)!");
        }
    }
    
    protected OntologyAccess fetchOntology( String ontologyID, String format, URI uri ) 
    	throws OntologyLoaderException {
    	OntologyAccess oa = null;
    	
        // check the format
        if ( "OBO".equals( format ) ) {
            if ( uri == null ) {
                throw new IllegalArgumentException( "The given CvSource doesn't have a URL" );
            } else {
                URL url;
                try {
                    url = uri.toURL();
                } catch ( MalformedURLException e ) {
                    throw new IllegalArgumentException( "The given CvSource doesn't have a valid URL: " + uri );
                }

                // parse the URL and load the ontology
                OboLoader loader = new OboLoader();
                try {
                    log.debug( "Parsing ontology at URL: " + url );
                    oa = loader.parseOboFile( url, ontologyID );
                    oa.setName(ontologyID);
                } catch ( Exception e ) {
                    throw new OntologyLoaderException( "OboFile parser failed with Exception: ", e );
                }
            }
        } else {
            throw new OntologyLoaderException( "Unsupported ontology format: " + format );
        }

        log.info( "Successfully created OntologyAccessImpl from values: ontology="
              + ontologyID + " format=" + format + " location=" + uri );
        
        return oa;
    }
    

	public Set<OntologyTermI> searchTermByName(String name) {
		return searchTermByName(name, null);
	}
	
	
	public Set<OntologyTermI> searchTermByName(String name, Set<String> ontologies) {
		Set<OntologyTermI> found  = new HashSet<OntologyTermI>();
		assert name!=null : "searchTermByName: null arg.";
		
		Set<String> ontologyIDs = new HashSet<String>(getOntologyIDs());
		if(ontologies != null && !ontologies.isEmpty())
			ontologyIDs.retainAll(ontologies);
		
		for(String ontologyId: ontologyIDs) {
			OntologyAccess oa = getOntology(ontologyId);
			for(OntologyTermI term : oa.getOntologyTerms()) {
				String prefName = term.getPreferredName();
				if(prefName == null) {
					log.error("searchTermByName: NULL preffered name for term " 
						+ term.getTermAccession() + " in " + ontologyId + "; report to authors.");
				} else if(name.equalsIgnoreCase(prefName)) {
					found.add(term);
				} else {
					for(String syn : term.getNameSynonyms()) {
						if(syn.equalsIgnoreCase(name)) {
							found.add(term);
						}
					}
				}
			}
		}
		
		return found;
	}
	
	
	public OntologyTermI findTermByAccession(String acc) {
		OntologyTermI term = null;
		
		for(String ontologyId : getOntologyIDs()) {
			term = getOntology(ontologyId).getTermForAccession(acc);
			if(term != null) 
				break;
		}
		
		return term;
	}
	
    /**
     * Collect all available names in the given collection of OntologyAccess terms.
     * @param terms the terms for which we want the names.
     * @return a non null collection of names.
     */
    protected static Collection<String> getTermNames(  Collection<OntologyTermI> terms ) {
        if ( terms == null ) {
            return Collections.emptyList();
        }
        Collection<String> names = new ArrayList<String>( terms.size() );
        for ( OntologyTermI term : terms ) {
            names.add( term.getPreferredName() );
            names.addAll( term.getNameSynonyms() );
        }
        return names;
    }

}