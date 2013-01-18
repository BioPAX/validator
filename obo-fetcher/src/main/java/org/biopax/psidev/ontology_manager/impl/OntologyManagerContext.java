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
            throw new IllegalArgumentException( "You must give a non null ontologyDirectory, " +
            		"use setStoreOntologiesLocally(boolean) to disable the long term storing." );
        }
        this.ontologyDirectory = ontologyDirectory;
        if (log.isInfoEnabled())
			log.info("Using ontologies cache directory : " + 
					this.ontologyDirectory.getAbsolutePath());
    }
}
