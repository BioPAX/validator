package psidev.psi.tools.ontology_manager.interfaces;

import java.util.Collection;

/**
 * Author: Florian Reisinger
 * Date: 09-Jul-2008
 */
public interface OntologyTermI {

    public String getTermAccession();

    public String getPreferredName();

    public void setTermAccession( String accession );

    public void setPreferredName( String preferredName );

    public Collection<String> getNameSynonyms();

    public void setNameSynonyms( Collection<String> nameSynonyms );
}
