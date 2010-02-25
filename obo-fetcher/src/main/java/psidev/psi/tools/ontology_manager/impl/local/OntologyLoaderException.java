/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package psidev.psi.tools.ontology_manager.impl.local;

/**
 * TODO comment this
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id: OntologyLoaderException.java 656 2007-06-29 11:18:19 +0100 (Fri, 29 Jun 2007) skerrien $
 * @since <pre>26-Apr-2006</pre>
 */
public class OntologyLoaderException extends Exception {
    public OntologyLoaderException( String message ) {
        super( message );
    }

    public OntologyLoaderException( String message, Throwable cause ) {
        super( message, cause );
    }
}