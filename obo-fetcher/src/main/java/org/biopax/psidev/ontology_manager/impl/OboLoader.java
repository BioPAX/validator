/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package org.biopax.psidev.ontology_manager.impl;

//import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.psidev.ontology_manager.Ontology;
import org.biopax.psidev.ontology_manager.OntologyTermI;
import org.biopax.uk.ac.ebi.ols.TermRelationship;
import org.biopax.uk.ac.ebi.ols.TermSynonym;
import org.biopax.uk.ac.ebi.ols.impl.BaseOBO2AbstractLoader;
import org.biopax.uk.ac.ebi.ols.impl.OBO2FormatParser;
import org.biopax.uk.ac.ebi.ols.impl.TermBean;




import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.*;

/**
 * Wrapper class that hides the way OLS handles OBO files.
 *
 * @author Samuel Kerrien
 * @version $Id: OboLoader.java 656 2007-06-29 11:18:19 +0100 (Fri, 29 Jun 2007) skerrien $
 * @since <pre>30-Sep-2005</pre>
 */
public class OboLoader extends BaseOBO2AbstractLoader {

    /**
     * Sets up a logger for that class.
     */
    public static final Log log = LogFactory.getLog( OboLoader.class );

    private static final String ONTOLOGY_REGISTRY_NAME = "ontology.registry.map";

    public OboLoader( ) {
    }


    //////////////////////////////
    // User's methods

    private Ontology buildOntology(String ontologyID) {

        Ontology ontology = new OntologyImpl();

        // 1. convert and index all terms (note: at this stage we don't handle the hierarchy)
        for ( Iterator iterator = ontBean.getTerms().iterator(); iterator.hasNext(); ) {
            TermBean term = ( TermBean ) iterator.next();

            /*
             * Quick workaround for that
             * we want to ignore the PSI-MOD terms that are included into PSI-MI files!
             */
            if("PSI-MOD".equals(term.getNamespace()) 
            		&& ("PSI-MI".equals(ontologyID) || "MI".equals(ontologyID)))
            	continue; // skip
            
            // convert term into a OboTerm
            OntologyTermI ontologyTerm = new OntologyTermImpl(ontologyID, term.getIdentifier(), 
            		term.getName() );
// the "unescape" workaround is not required anymore - after obo-fetcher internal implementation changed!
//            		StringEscapeUtils.unescapeXml(term.getName()) ); 
//            //- unescapeXml above and below is a workaround the bug in ols-1.18 OBO parser (org.biopax.uk.ac.ebi.ols.loader..), 
//            // which returns, e.g., "O4&;apos;-phospho-L-tyrosine" instead "O4'-phospho-L-tyrosine")
            
            final Collection<TermSynonym> synonyms = (Collection<TermSynonym>) term.getSynonyms();
            if( synonyms != null ) {
                for ( TermSynonym synonym : synonyms ) {
//                    ontologyTerm.getNameSynonyms().add( StringEscapeUtils.unescapeXml(synonym.getSynonym()) );
                    ontologyTerm.getNameSynonyms().add( synonym.getSynonym() );
                }
            }

            ontology.addTerm( ontologyTerm );

            if ( term.isObsolete() ) {
                ontology.addObsoleteTerm( ontologyTerm );
            }
        }

        // 2. build hierarchy based on the relations of the Terms
        for ( Iterator iterator = ontBean.getTerms().iterator(); iterator.hasNext(); ) {
            TermBean term = ( TermBean ) iterator.next();

            /*
             * Quick workaround an issue that
             * we want to ignore PSI-MOD included in PSI-MI
             */
            if("PSI-MOD".equals(term.getNamespace()) 
            		&& ("PSI-MI".equals(ontologyID) || "MI".equals(ontologyID)))
            	continue; // skip
            
            if ( term.getRelationships() != null ) {
                for ( Iterator iterator1 = term.getRelationships().iterator(); iterator1.hasNext(); ) {
                    TermRelationship relation = ( TermRelationship ) iterator1.next();
                    
                   // one more step to ignore PSI-MOD included in PSI-MI
                   /*
                    String nso = relation.getObjectTerm().getNamespace();
                    String nss = relation.getSubjectTerm().getNamespace();
                    if(("PSI-MI".equals(ontologyID) || "MI".equals(ontologyID)))
                    	if("PSI-MOD".equals(nso) || "PSI-MOD".equals(nss)
                    		|| "MOD".equals(nso) || "MOD".equals(nss))
                    		continue; // skip the external relation
                    */
                    // - better simply to check for NPE - 
                    try {
                    	ontology.addLink( relation.getObjectTerm().getIdentifier(),
                    					  relation.getSubjectTerm().getIdentifier() );
                    } catch (NullPointerException e) {
                    	if(log.isWarnEnabled())
                    		log.warn("Skipping terms relationship "  
                    			+ relation + "; " + e);
					}
                }
            }
        }

        return ontology;
    }

    /**
     * Parse the given OBO file and build a representation of the DAG into an IntactOntology.
     *
     * @param file the input file. It has to exist and to be readable, otherwise it will break.
     * @return a non null IntactOntology.
     */
    public Ontology parseOboFile( File file, String ontologyID) {

        if ( !file.exists() ) {
            throw new IllegalArgumentException( file.getAbsolutePath() + " doesn't exist." );
        }

        if ( !file.canRead() ) {
            throw new IllegalArgumentException( file.getAbsolutePath() + " could not be read." );
        }

        try {
            setParser(new OBO2FormatParser(file.getAbsolutePath()));
            process();
        } catch ( Exception e ) {
            log.fatal( "Parse failed: " + e.getMessage(), e );
        }

        return buildOntology(ontologyID);
    }

    
    private File getRegistryFile() throws OntologyLoaderException {
        File ontologyDirectory = OntologyManagerContext.getInstance().getOntologyDirectory();

        File[] registry = ontologyDirectory.listFiles( new FileFilter() {
            public boolean accept( File pathname ) {
                return ONTOLOGY_REGISTRY_NAME.equals( pathname.getName() );
            }
        } );

        if ( registry.length == 1 ) {
            // found our file
            File validatorRegistry = registry[0];
            return validatorRegistry;
        } else {
            // create it
            return new File( ontologyDirectory.getAbsolutePath() + File.separator + ONTOLOGY_REGISTRY_NAME );
        }
    }

    /**
     * Load an OBO file from an URL.
     *
     * @param url the URL to load (must not be null)
     * @return an ontology
     * @see #parseOboFile(File file)
     */
    public Ontology parseOboFile( URL url, String ontologyID ) throws OntologyLoaderException {

        // load config file (ie. a map)
        // check if that URL has already been loaded
        // if so, get the associated temp file and check if available
        // if available, then load it and skip URL load
        // if any of the above failed, load it from the network.

        if ( url == null ) {
            throw new IllegalArgumentException( "Please give a non null URL." );
        }


        File ontologyFile = null;
        File ontologyDirectory = OntologyManagerContext.getInstance().getOntologyDirectory();
        boolean isKeepDownloadedOntologiesOnDisk = OntologyManagerContext.getInstance().isStoreOntologiesLocally();
        Map registryMap = null;

        if( isKeepDownloadedOntologiesOnDisk ) {

            if ( ontologyDirectory == null ) {
                throw new IllegalArgumentException( "Ontology directory cannot be null, " +
                		"please set it using OntologyManagerContext" );
            }

            if ( !ontologyDirectory.exists() ) {
                throw new IllegalArgumentException( "Ontology directory " + 
                		ontologyDirectory.getPath() + " must exist" );
            }

            if ( !ontologyDirectory.canWrite() ) {
                throw new IllegalArgumentException( "Ontology directory " +
                		ontologyDirectory.getPath() + " must be writeable" );
            }

            if ( log.isInfoEnabled() ) {
                log.info( "User work directory: " + ontologyDirectory.getAbsolutePath() );
                log.info( "keepTemporaryFile: " + OntologyManagerContext.getInstance().isStoreOntologiesLocally() );
            }

            File registryFile = getRegistryFile();

            if ( null != registryFile ) {

                // deserialise the Map
                try {
                    if ( registryFile.length() > 0 ) {
                        // the file has some content
                        ObjectInputStream ois = new ObjectInputStream( new FileInputStream( registryFile ) );
                        registryMap = ( Map ) ois.readObject();

                        if ( registryMap != null ) {
                            if ( registryMap.containsKey( url ) ) {
                                ontologyFile = new File( ( String ) registryMap.get( url ) );

                                if ( ontologyFile.exists() && ontologyFile.canRead() ) {

                                    // Cool, find it ! use it instead of the provided URL
                                    if ( log.isInfoEnabled() )
                                        log.info( "Reuse existing cache: " + ontologyFile.getAbsolutePath() );

                                } else {

                                    if ( log.isInfoEnabled() )
                                        log.info( "Could not find " + ontologyFile.getAbsolutePath() );

                                    // cleanup map
                                    registryMap.remove( url );

                                    // save map
                                    log.info( "Saving registry file..." );
                                    File f = getRegistryFile();
                                    ObjectOutputStream oos = new ObjectOutputStream( new FileOutputStream( f ) );
                                    oos.writeObject( registryMap );
                                    oos.flush();
                                    oos.close();
                                }
                            }
                        } else {
                            log.info( "could not deserialize the Map" );
                        }
                    } else {
                        log.info( "The file is empty" );
                    }
                } catch ( IOException e ) {
                    // optional, so just display message in the log
                    log.error( "Error while deserializing the map", e );
                } catch ( ClassNotFoundException e ) {
                    // optional, so just display message in the log
                    log.error( "Error while deserializing the map", e );
                }
            }
        }


        try {
            if ( ontologyFile == null || !ontologyFile.exists() || !ontologyFile.canRead() ) {

                // if it is not defined, not there or not readable...

                // Read URL content
                if ( log.isInfoEnabled() ) log.info( "Loading URL: " + url );

                URLConnection con = url.openConnection();
                long size = con.getContentLength();        // -1 if not stat available

                if ( log.isInfoEnabled() ) log.info( "size = " + size );

                InputStream is = url.openStream();

                // Create a temp file and write URL content in it.
                if ( !ontologyDirectory.exists() ) {
                    if ( !ontologyDirectory.mkdirs() ) {
                        throw new IOException( "Cannot create temp directory: " + ontologyDirectory.getAbsolutePath() );
                    }
                }

                // make the temporary file name specific to the URL
                String name = null;
                String filename = url.getFile();
                int idx = filename.lastIndexOf( '/' );
                if ( idx != -1 ) {
                    name = filename.substring( idx+1, filename.length() );
                    name = name.replaceAll("[.,;:&^%$@*?=]", "_");
                } else {
                    name = "unknown";
                }

                // build the file
                ontologyFile = new File( ontologyDirectory + File.separator + name + System.currentTimeMillis() + ".obo" );
                if ( ! isKeepDownloadedOntologiesOnDisk ) {
                    log.info( "Request file to be deleted on exit." );
                    ontologyFile.deleteOnExit();
                }

                if ( log.isDebugEnabled() )
                    log.debug( "The OBO file will be temporary stored as: " + ontologyFile.getAbsolutePath() );

                FileOutputStream out = new FileOutputStream( ontologyFile );

                //not very efficient -
                /* 
                int length = 0;
                int current = 0;
                byte[] buf = new byte[1024 * 1024];

                while ( ( length = is.read( buf ) ) != -1 ) {
                    current += length;
                    out.write( buf, 0, length );
                    if ( log.isInfoEnabled() ) {
                        log.info( "length = " + current );
                        if(size > 0)
                        	log.info( "Percent: " 
                        		+ ( ( current / ( float ) size ) * 100 ) + "%" );
                    }
                }
                */
                
                if(size == -1) size = 1024 * 1024 * 1024; //Integer.MAX_VALUE;
                ReadableByteChannel source = Channels.newChannel(is);
				size = out.getChannel().transferFrom(source, 0, size);
				if(log.isInfoEnabled())
					log.info(size + " bytes downloaded");

                is.close();
                out.flush();
                out.close();

                if ( isKeepDownloadedOntologiesOnDisk ) {
                    // if the user has requested for the ontology file to be kept, store file reference in the registry
                    if ( registryMap == null ) {
                        registryMap = new HashMap();
                    }

                    registryMap.put( url, ontologyFile.getAbsolutePath() );

                    // serialize the map
                    if ( log.isInfoEnabled() ) log.info( "Serializing Map" );
                    File f = getRegistryFile();
                    ObjectOutputStream oos = new ObjectOutputStream( new FileOutputStream( f ) );
                    oos.writeObject( registryMap );
                    oos.flush();
                    oos.close();
                }
            }

            if ( ontologyFile == null ) {
                log.error( "The ontology file is still null..." );
            }

            // Parse file
            return parseOboFile( ontologyFile, ontologyID );

        } catch ( IOException e ) {
            throw new OntologyLoaderException( "Error while loading URL (" + url + ")", e );
        }
    }
}