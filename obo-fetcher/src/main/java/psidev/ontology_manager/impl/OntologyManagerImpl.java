package psidev.ontology_manager.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import psidev.ontology_manager.Ontology;
import psidev.ontology_manager.OntologyManager;
import psidev.ontology_manager.OntologyTermI;
import psidev.psi.tools.ontologyCfgReader.mapping.jaxb.CvSource;
import psidev.psi.tools.ontologyCfgReader.mapping.jaxb.CvSourceList;
import psidev.psi.tools.ontologyConfigReader.OntologyConfigReader;
import psidev.psi.tools.ontologyConfigReader.OntologyConfigReaderException;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Central access to configured Ontology.
 *
 * @author Florian Reisinger
 * @Samuel Kerrien (skerrien@ebi.ac.uk)
 * @author rodche (baderlab.org)
 * @since 2.0.0
 */
public class OntologyManagerImpl implements OntologyManager {

    public static final Log log = LogFactory.getLog( OntologyManagerImpl.class );

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
        if ( log.isDebugEnabled() ) 
        	log.info( "Created new unconfigured OntologyManagerImpl." );
    }

    /**
     * Creates a new OntologyManagerImpl managing the ontologies specified in the config file.
     * This config file has to be defined as per the following XSD:
     * <pre>http://www.psidev.info/files/validator/CvSourceList.xsd</pre>
     *
     * @param configFile configuration file for the manager.
     * @throws OntologyLoaderException if the config file could not be parsed or the loading of a ontology failed.
     */
	public OntologyManagerImpl(InputStream configFile)
			throws OntologyLoaderException 
	{
		ontologies = new HashMap<String, Ontology>();
		loadOntologies(configFile);
		if (log.isDebugEnabled())
			log.debug("Successfully created and configured new OntologyManagerImpl.");
	}

    ////////////////////
    // public methods

    /* (non-Javadoc)
	 * @see psidev.ontology_manager.OntologyManager#putOntology(java.lang.String, psidev.ontology_manager.interfaces.Ontology)
	 */
    /* (non-Javadoc)
	 * @see psidev.ontology_manager.impl.OntologyManager#putOntology(java.lang.String, psidev.ontology_manager.interfaces.Ontology)
	 */
    public Ontology putOntology( String ontologyID, Ontology ontology ) {
        if ( ontologies.containsKey( ontologyID ) ) {
            if ( log.isWarnEnabled() )log.warn( "Ontology with the ID '" + ontologyID + "' already exists. Overwriting!" );
        }
        return ontologies.put( ontologyID, ontology );
    }

    /* (non-Javadoc)
	 * @see psidev.ontology_manager.OntologyManager#getOntologyIDs()
	 */
    /* (non-Javadoc)
	 * @see psidev.ontology_manager.impl.OntologyManager#getOntologyIDs()
	 */
    public Set<String> getOntologyIDs() {
        return ontologies.keySet();
    }

    /* (non-Javadoc)
	 * @see psidev.ontology_manager.OntologyManager#getOntologyAccess(java.lang.String)
	 */
    /* (non-Javadoc)
	 * @see psidev.ontology_manager.impl.OntologyManager#getOntologyAccess(java.lang.String)
	 */
    public Ontology getOntology( String ontologyID ) {
        return ontologies.get( ontologyID );
    }

    /* (non-Javadoc)
	 * @see psidev.ontology_manager.OntologyManager#setOntologyDirectory(java.io.File)
	 */
    /* (non-Javadoc)
	 * @see psidev.ontology_manager.impl.OntologyManager#setOntologyDirectory(java.io.File)
	 */
    public void setOntologyDirectory( File ontologyDirectory ) {
        OntologyManagerContext.getInstance().setOntologyDirectory( ontologyDirectory );
    }

    /* (non-Javadoc)
	 * @see psidev.ontology_manager.OntologyManager#containsOntology(java.lang.String)
	 */
    /* (non-Javadoc)
	 * @see psidev.ontology_manager.impl.OntologyManager#containsOntology(java.lang.String)
	 */
    public boolean containsOntology( String ontologyID ) {
        return ontologies.containsKey( ontologyID );
    }

    /* (non-Javadoc)
	 * @see psidev.ontology_manager.OntologyManager#loadOntologies(java.io.InputStream)
	 */
    /* (non-Javadoc)
	 * @see psidev.ontology_manager.impl.OntologyManager#loadOntologies(java.io.InputStream)
	 */
    public void loadOntologies( InputStream configFile ) throws OntologyLoaderException {

        OntologyConfigReader ocr = new OntologyConfigReader();
        final CvSourceList cvSourceList;
        try {
            cvSourceList = ocr.read( configFile );
        } catch ( OntologyConfigReaderException e ) {
            throw new OntologyLoaderException( "Error while reading ontology config file", e );
        }

        if ( cvSourceList != null ) {
            for ( CvSource cvSource : cvSourceList.getCvSource() ) {

                String sourceUri = cvSource.getUri();
                final String id = cvSource.getIdentifier();
                final String name = cvSource.getName();
                final String version = cvSource.getVersion();
                final String format = cvSource.getFormat();
                final String loaderClass = cvSource.getSource();

                URI uri;
                try {

                    if ( sourceUri != null && sourceUri.toLowerCase().startsWith( CLASSPATH_PREFIX ) ) {
                        sourceUri = sourceUri.substring( CLASSPATH_PREFIX.length() );
                        if ( log.isDebugEnabled() ) {
                            log.debug( "Loading ontology from classpath: " + sourceUri );
                        }
                        final URL url = OntologyManagerImpl.class.getClassLoader().getResource( sourceUri );
                        if ( url == null ) {
                            throw new OntologyLoaderException( "Unable to load from classpath: " + sourceUri );
                        }
                        uri = url.toURI();
                        if ( log.isDebugEnabled() ) {
                            log.debug( "URI=" + uri.toASCIIString() );
                        }

                    } else {
                        uri = new URI( sourceUri );
                    }

                } catch ( URISyntaxException e ) {
                    throw new IllegalArgumentException( "The specified uri '" + sourceUri + "' " +
                                                        "for ontology '" + id + "' has an invalid syntax.", e );
                }

                if ( log.isInfoEnabled() ) {
                    log.info( "Loading ontology: name=" + name + ", ID= " + id + ", format=" + format
                              + ", version=" + version + ", uri=" + uri + " using source: " + loaderClass );
                }


                try {
                    
                    Ontology oa = fetchOntology( id, name, version, format, uri );
                    putOntology(id, oa);
                    
                } catch ( Exception e ) {
                    throw new OntologyLoaderException( "Failed loading ontology source: " + loaderClass, e );
                }
            }
        }
    }
    

    /* (non-Javadoc)
	 * @see psidev.ontology_manager.OntologyManager#fetchOntology(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.net.URI)
	 */
    /* (non-Javadoc)
	 * @see psidev.ontology_manager.impl.OntologyManager#fetchOntology(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.net.URI)
	 */
    public Ontology fetchOntology( String ontologyID, String name, String version, String format, URI uri ) 
    	throws OntologyLoaderException {
    	Ontology oa = null;
    	
        // first check the format
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
                    if ( log.isDebugEnabled() ) {
                        log.debug( "Parsing URL: " + url );
                    }
                    oa = loader.parseOboFile( url, ontologyID );
                    oa.setName(name);
                } catch ( OntologyLoaderException e ) {
                    throw new OntologyLoaderException( "OboFile parser failed with Exception: ", e );
                }
            }
        } else {
            throw new OntologyLoaderException( "Unsupported ontology format: " + format );
        }

        if ( log.isInfoEnabled() ) {
            log.info( "Successfully created OntologyImpl from values: ontology="
                      + ontologyID + " name=" + name + " version=" + version + " format=" + format + " location=" + uri );
        }
        
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