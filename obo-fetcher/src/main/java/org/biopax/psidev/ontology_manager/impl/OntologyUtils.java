/**
 * Copyright 2008 The European Bioinformatics Institute, and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import java.util.Collections;

import org.biopax.psidev.ontology_manager.OntologyTermI;


/**
 * Ontology utils.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 2.0.0
 */
public class OntologyUtils {

    /**
     * Collect all available accessions in the given collection of Ontology terms.
     * @param terms the terms for which we want the accessions.
     * @return a non null collection of accession.
     */
    public static Collection<String> getAccessions(  Collection<OntologyTermI> terms ) {
        if ( terms == null ) {
            return Collections.emptyList();
        }
        Collection<String> accessions = new ArrayList<String>( terms.size() );
        for ( OntologyTermI term : terms ) {
            accessions.add( term.getTermAccession() );
        }
        return accessions;
    }

    /**
     * Collect all available names in the given collection of Ontology terms.
     * @param terms the terms for which we want the names.
     * @return a non null collection of names.
     */
    public static Collection<String> getTermNames(  Collection<OntologyTermI> terms ) {
        if ( terms == null ) {
            return Collections.emptyList();
        }
        Collection<String> names = new ArrayList<String>( terms.size() );
        for ( OntologyTermI term : terms ) {
            names.add( term.getPreferredName() );
            names.addAll( term.getNameSynonyms() );
        }
        return names;
    }
}
