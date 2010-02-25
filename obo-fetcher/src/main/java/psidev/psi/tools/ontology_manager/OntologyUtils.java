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
package psidev.psi.tools.ontology_manager;

import psidev.psi.tools.ontology_manager.interfaces.OntologyTermI;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Collections;

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
            return Collections.EMPTY_LIST;
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
            return Collections.EMPTY_LIST;
        }
        Collection<String> names = new ArrayList<String>( terms.size() );
        for ( OntologyTermI term : terms ) {
            names.add( term.getPreferredName() );
            names.addAll( term.getNameSynonyms() );
        }
        return names;
    }
}
