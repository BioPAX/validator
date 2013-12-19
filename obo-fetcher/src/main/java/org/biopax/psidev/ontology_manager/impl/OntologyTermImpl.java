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



import java.util.Collection;
import java.util.ArrayList;

import org.biopax.psidev.ontology_manager.OntologyTermI;

/**
 * Representation of a cv term.
 *
 * @author Florian Reisinger (florian@ebi.ac.uk)
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 2.0.0
 */
public class OntologyTermImpl implements OntologyTermI {

    private String acc;

    private String name;

    private Collection<String> nameSynonyms;
    
    private String ontologyId;

    //////////////////////////
    // Constructors

    public OntologyTermImpl( String acc ) {
        setTermAccession( acc );
    }

    public OntologyTermImpl( String ont, String acc, String name ) {
    	this(acc);
    	this.name = name;
        this.ontologyId = ont;
    }
    
    ///// ///// ///// ///// /////
    // Getter & Setter

    public void setTermAccession( String accession ) {
        if ( accession == null || accession.trim().length() == 0 ) {
            throw new IllegalArgumentException( "You must give a non null/empty term accession" );
        }
        acc = accession;
    }

    public String getTermAccession() {
        return acc;
    }

    public void setPreferredName( String preferredName ) {
        name = preferredName;
    }

    public Collection<String> getNameSynonyms() {
        if ( nameSynonyms == null ) {
            nameSynonyms = new ArrayList();
        }
        return nameSynonyms;
    }

    public void setNameSynonyms( Collection<String> nameSynonyms ) {
        this.nameSynonyms = nameSynonyms;
    }

    public String getPreferredName() {
        return name;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append( "OntologyTermImpl" );
        sb.append( "{acc='" ).append( acc ).append( '\'' );
        sb.append( ", name='" ).append( name ).append( '\'' );
        sb.append( '}' );
        return sb.toString();
    }

    @Override
    public boolean equals( Object o ) {
        if ( this == o ) return true;
        if ( o == null || getClass() != o.getClass() ) return false;

        OntologyTermImpl that = ( OntologyTermImpl ) o;

        if ( !acc.equals( that.acc ) ) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        result = acc.hashCode();
        return result;
    }

	public String getOntologyId() {
		return this.ontologyId;
	}

	public void setOntologyId(String ontologyId) {
		this.ontologyId = ontologyId;
	}
}
