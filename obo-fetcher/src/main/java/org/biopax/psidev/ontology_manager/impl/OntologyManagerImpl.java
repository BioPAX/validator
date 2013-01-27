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
import org.biopax.psidev.ontology_manager.Ontology;
import org.biopax.psidev.ontology_manager.OntologyManager;
import org.biopax.psidev.ontology_manager.OntologyTermI;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;


import java.io.File;
import java.net.*;
import java.util.*;

/**
 * Central access to configured Ontology.
 *
 * @author Florian Reisinger
 * @Samuel Kerrien (skerrien@ebi.ac.uk)
 * @author rodche (baderlab.org) - re-factored for the BioPAX Validator
 * @since 2.0.0
 */
public class OntologyManagerImpl implements OntologyManager {

    public static final Log log = LogFactory.getLog( OntologyManagerImpl.class );
   
    private static final ResourceLoader LOADER = new DefaultResourceLoader();
    
    /**
     * The Map that holds the Ontologies.
     * The key is the ontology ID and the value is a ontology inplementing the Ontology interface.
     */
    private Map<String, Ontology> ontologies;

    /**
     * Create a new OntologyManagerImpl with no configuration (no associated ontologies).
     */
    public OntologyManagerImpl() {
        ontologies = new HashMap<String, Ontology>();
    }

    /** 
     * Creates a new OntologyManagerImpl managing the ontologies specified in the config map.
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


    public Ontology putOntology( String ontologyID, Ontology ontology ) {
        if ( ontologies.containsKey( ontologyID ) ) {
            log.warn( "Ontology with the ID '" + ontologyID + "' already exists. Overwriting!" );
        }
        return ontologies.put( ontologyID, ontology );
    }


    public Set<String> getOntologyIDs() {
        return ontologies.keySet();
    }


    public Ontology getOntology( String ontologyID ) {
        return ontologies.get( ontologyID );
    }


    public void setOntologyDirectory( File ontologyDirectory ) {
        OntologyManagerContext.getInstance().setOntologyDirectory( ontologyDirectory );
    }


    public boolean containsOntology( String ontologyID ) {
        return ontologies.containsKey( ontologyID );
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

                    Ontology oa = fetchOntology( key, "OBO", uri );
                    putOntology(key, oa);
                } catch ( Throwable e ) { //using Throwable because StackOverflowError is also possible here
                    throw new OntologyLoaderException("Failed loading/parsing ontology " + key 
                    	+ " from " + config.getProperty(key), e );
                }
            }
        } else {
        	throw new OntologyLoaderException("Ontology configuration map is missing or empty (map)!");
        }
    }
    
    protected Ontology fetchOntology( String ontologyID, String format, URI uri ) 
    	throws OntologyLoaderException {
    	Ontology oa = null;
    	
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
                OboLoader loader = new OboLoader( );
                try {
                    log.debug( "Parsing URL: " + url );
                    oa = loader.parseOboFile( url, ontologyID );
                    oa.setName(ontologyID);
                } catch ( Exception e ) {
                    throw new OntologyLoaderException( "OboFile parser failed with Exception: ", e );
                }
            }
        } else {
            throw new OntologyLoaderException( "Unsupported ontology format: " + format );
        }

        log.info( "Successfully created OntologyImpl from values: ontology="
              + ontologyID + " format=" + format + " location=" + uri );
        
        return oa;
    }
    

	public Set<OntologyTermI> searchTermByName(String name) {
		Set<OntologyTermI> found  = new HashSet<OntologyTermI>();
		
		for(String ontologyId: getOntologyIDs()) {
			Ontology oa = getOntology(ontologyId);
			for(OntologyTermI term : oa.getOntologyTerms()) {
				if(term.getPreferredName().equalsIgnoreCase(name)) {
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

}