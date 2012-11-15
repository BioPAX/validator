package org.biopax.psidev.ontology_manager.impl;


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

	@Override
	public String getOntologyId() {
		return this.ontologyId;
	}

	@Override
	public void setOntologyId(String ontologyId) {
		this.ontologyId = ontologyId;
	}
}
