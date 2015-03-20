package org.biopax.psidev.ontology_manager;

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
import java.util.Set;

/**
 * Defines what can be asked to an ontology.
 *
 * @author Florian Reisinger
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @author rodche (baberlab.org) - re-factoring
 * @since 2.0.0
 */
public interface OntologyAccess {

    /**
     * This method builds a set of all allowed terms based on the specified parameters.
     *
     * @param accession     the accession number of the term wanted.
     * @param allowChildren whether child terms are allowed.
     * @param useTerm       whether to include the specified term ID in the set of allowed IDs.
     * @return a non null set of allowed ontology terms for the specified parameters.
     */
    public Set<OntologyTermI> getValidTerms( String accession, boolean allowChildren, boolean useTerm );

    /**
     * Search a term by accession number.
     *
     * @param accession the accession to be searched for.
     * @return an ontology term or null if not found.
     */
    public OntologyTermI getTermForAccession( String accession );
    
    void setName(String name);
       
    String getName();
    
    boolean hasTerms();

    OntologyTermI search( String id );

    Collection<OntologyTermI> getRoots();

    Collection<OntologyTermI> getOntologyTerms();

    Collection<OntologyTermI> getObsoleteTerms();

    boolean isObsolete( OntologyTermI term );

    Set<OntologyTermI> getDirectParents( OntologyTermI term );

    Set<OntologyTermI> getDirectChildren( OntologyTermI term );

    Set<OntologyTermI> getAllParents( OntologyTermI term );

    Set<OntologyTermI> getAllChildren( OntologyTermI term );
    
    void addLink( String parentId, String childId );
    
    void addTerm( OntologyTermI term );
    
    void addObsoleteTerm( OntologyTermI term );

}