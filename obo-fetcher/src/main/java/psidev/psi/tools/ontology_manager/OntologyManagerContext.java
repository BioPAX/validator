package psidev.psi.tools.ontology_manager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;

/**
 * Ontology manager context that is only valid for the current thread (uses ThreadLocal)
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 2.0.1
 */
public class OntologyManagerContext {

    public static final Log log = LogFactory.getLog( OntologyManagerContext.class );

    private File ontologyDirectory;

    private boolean storeOntologiesLocally;


    private static ThreadLocal<OntologyManagerContext> instance =
            new ThreadLocal<OntologyManagerContext>() {
                @Override
                protected OntologyManagerContext initialValue() {
                    return new OntologyManagerContext();
                }
            };

    public static OntologyManagerContext getInstance() {
        return instance.get();
    }

    private OntologyManagerContext() {
        // initialize here default configuration
        storeOntologiesLocally = false;
        ontologyDirectory = new File( System.getProperty( "java.io.tmpdir" ) );
        if ( log.isDebugEnabled() ) {
            log.debug( "Default directory for storing ontologies set to " + ontologyDirectory.getAbsolutePath() );
        }
    }

    ///////////////////////////
    // Getters and Setters

    public boolean isStoreOntologiesLocally() {
        return storeOntologiesLocally;
    }

    public void setStoreOntologiesLocally( boolean storeOntologiesLocally ) {
        this.storeOntologiesLocally = storeOntologiesLocally;
    }

    public File getOntologyDirectory() {
        return ontologyDirectory;
    }

    public void setOntologyDirectory( File ontologyDirectory ) {
        if ( ontologyDirectory == null ) {
            throw new IllegalArgumentException( "You must give a non null ontologyDirectory, use setStoreOntologiesLocally(boolean) to disable the long term storing." );
        }
        this.ontologyDirectory = ontologyDirectory;
    }
}
