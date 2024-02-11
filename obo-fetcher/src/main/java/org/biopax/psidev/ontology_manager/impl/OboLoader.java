package org.biopax.psidev.ontology_manager.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.biopax.ols.TermRelationship;
import org.biopax.ols.TermSynonym;
import org.biopax.ols.impl.BaseOBO2AbstractLoader;
import org.biopax.ols.impl.OBO2FormatParser;
import org.biopax.ols.impl.TermBean;
import org.biopax.psidev.ontology_manager.OntologyAccess;
import org.biopax.psidev.ontology_manager.OntologyTermI;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

/**
 * Wrapper class that hides the way OLS handles OBO files.
 *
 * @author Samuorg.biopax.validator.*el Kerrien
 * @version $Id: OboLoader.java 656 2007-06-29 11:18:19 +0100 (Fri, 29 Jun 2007) skerrien $
 * @since <pre>30-Sep-2005</pre>
 */
public class OboLoader extends BaseOBO2AbstractLoader {
    public static final Logger log = LoggerFactory.getLogger( OboLoader.class );

    public OboLoader( ) {
    }

    /**
     * Parse the given OBO file and build a representation of the DAG into an IntactOntology.
     *
     * @param file the input file (has to exist and be readable)
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
            log.error("Parse failed", e);
        }
        return buildOntology(ontologyID);
    }


    /**
     * Load an OBO data from URL (uses a temporary local file).
     * Can read from a https:, file:, jar:file: (classpath) URL.
     *
     * @param url of the resource to load (not null)
     * @return ontology access object
     * @see #parseOboFile(File, String)
     */
    public OntologyAccess parseOboFile(URL url, String ontologyID ) throws Exception {
        if ( url == null ) {
            throw new IllegalArgumentException( "URL is null" );
        }
        log.info( "Loading OBO data from URI: " + url );
        try {
            Path tmp = Files.createTempFile(ontologyID + "_", ".obo");
            tmp.toFile().deleteOnExit();
            log.info("Using a temporary OBO file: " + tmp);
            Files.copy(url.openStream(), tmp, StandardCopyOption.REPLACE_EXISTING);
            // process the temporary OBO file
            return parseOboFile(tmp.toFile(), ontologyID);
        } catch ( IOException e ) {
            throw new OntologyLoaderException( "Failed to parse OBO data from URI", e );
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
            
            final Collection<TermSynonym> synonyms = term.getSynonyms();
            if( synonyms != null ) {
                for ( TermSynonym synonym : synonyms ) {
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