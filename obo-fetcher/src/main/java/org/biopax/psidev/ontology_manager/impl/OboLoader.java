/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package org.biopax.psidev.ontology_manager.impl;

/*
 *
 */

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.ols.TermRelationship;
import org.biopax.ols.TermSynonym;
import org.biopax.ols.impl.BaseOBO2AbstractLoader;
import org.biopax.ols.impl.OBO2FormatParser;
import org.biopax.ols.impl.TermBean;
import org.biopax.psidev.ontology_manager.OntologyAccess;
import org.biopax.psidev.ontology_manager.OntologyTermI;




import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
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


    /**
     * Parse the given OBO file and build a representation of the DAG into an IntactOntology.
     *
     * @param file the input file. It has to exist and to be readable, otherwise it will break.
     * @return a non null IntactOntology.
     */
    public OntologyAccess parseOboFile( File file, String ontologyID) {

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
    
    
    /**
     * Load an OBO file from an URL.
     *
     * @param url the URL to load (must not be null)
     * @return an ontology
     * @see #parseOboFile(File, String)
     */
    public OntologyAccess parseOboFile( URL url, String ontologyID ) throws OntologyLoaderException {
        // load config file (ie. a map)
        // check if that URL has already been loaded
        // if so, get the associated temp file and check if available
        // if available, then load it and skip URL load
        // if any of the above failed, load it from the network.

        if ( url == null ) {
            throw new IllegalArgumentException( "Please give a non null URL." );
        }

        File ontologyFile = null;

        try {
            if ( ontologyFile == null || !ontologyFile.exists() || !ontologyFile.canRead() ) {
                // if it is not defined, not there or not readable...
                // Read URL content
                log.info( "Loading URL: " + url );
                URLConnection con = url.openConnection();
                long size = con.getContentLength(); // -1 if not stat available
                log.info( "size = " + size );

                InputStream is = url.openStream();

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

                ontologyFile = Files.createTempFile(name + "_", ".obo").toFile();
                ontologyFile.deleteOnExit();
                log.debug( "The OBO file will be temporary stored as: " + ontologyFile.getAbsolutePath() );

                FileOutputStream out = new FileOutputStream( ontologyFile );
                if(size == -1) size = 1024 * 1024 * 1024; //Integer.MAX_VALUE;
                ReadableByteChannel source = Channels.newChannel(is);
				size = out.getChannel().transferFrom(source, 0, size);
				log.info(size + " bytes downloaded");

                is.close();
                out.flush();
                out.close();
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
    
    
    private OntologyAccess buildOntology(String ontologyID) {

        OntologyAccess ontologyAccess = new OntologyAccessImpl();

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
            OntologyTermI ontologyTerm = new OntologyTermImpl(ontologyID, term.getIdentifier(), term.getName());
// this workaround is not required anymore (since obo-fetcher impl. has changed).
//          StringEscapeUtils.unescapeXml(term.getName()) );
//          // unescapeXml above and below was a workaround a bug in ols-1.18 OBO parser (org.biopax.ols.loader..),
//          // which returned, e.g., "O4&;apos;-phospho-L-tyrosine" instead "O4'-phospho-L-tyrosine")
            
            final Collection<TermSynonym> synonyms = (Collection<TermSynonym>) term.getSynonyms();
            if( synonyms != null ) {
                for ( TermSynonym synonym : synonyms ) {
//                    ontologyTerm.getNameSynonyms().add( StringEscapeUtils.unescapeXml(synonym.getSynonym()) );
                    ontologyTerm.getNameSynonyms().add( synonym.getSynonym() );
                }
            }

            ontologyAccess.addTerm( ontologyTerm );

            if ( term.isObsolete() ) {
                ontologyAccess.addObsoleteTerm( ontologyTerm );
            }
        }

        // 2. build hierarchy based on the relations of the Terms
        for ( Iterator iterator = ontBean.getTerms().iterator(); iterator.hasNext(); )
        {
            TermBean term = ( TermBean ) iterator.next();
            // a quick workaround an issue that we want to ignore PSI-MOD included in PSI-MI
            if("PSI-MOD".equals(term.getNamespace()) && ("PSI-MI".equals(ontologyID) || "MI".equals(ontologyID)))
            	continue; // skip
            
            if ( term.getRelationships() != null ) {
                for ( Iterator iterator1 = term.getRelationships().iterator(); iterator1.hasNext(); ) {
                    TermRelationship relation = ( TermRelationship ) iterator1.next();

                    // to ignore PSI-MOD included in PSI-MI, simply to check for NPE -
                    try {
                    	ontologyAccess.addLink( relation.getObjectTerm().getIdentifier(),
                                relation.getSubjectTerm().getIdentifier() );
                    } catch (NullPointerException e) {
                   		log.warn("Skipping terms relationship " + relation + "; " + e);
					}
                }
            }
        }

        return ontologyAccess;
    }
}