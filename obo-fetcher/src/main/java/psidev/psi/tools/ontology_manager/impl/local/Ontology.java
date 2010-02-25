package psidev.psi.tools.ontology_manager.impl.local;

import psidev.psi.tools.ontology_manager.interfaces.OntologyTermI;

import java.util.Collection;
import java.util.Set;

/**
 * <b> Behaviour of an Ontology.</b>
 * <p/>
 *
 * @author Samuel Kerrien
 * @author Matthias Oesterheld
 * @version $Id: Ontology.java 656 2007-06-29 11:18:19 +0100 (Fri, 29 Jun 2007) skerrien $
 * @since 1.0
 */
public interface Ontology {

    boolean hasTerms();

    OntologyTermI search( String id );

    Collection<OntologyTermI> getRoots();

    Collection<OntologyTermI> getOntologyTerms();

    Collection<OntologyTermI> getObsoleteTerms();

    boolean isObsoleteTerm( OntologyTermI term );

    Set<OntologyTermI> getDirectParents( OntologyTermI term );

    Set<OntologyTermI> getDirectChildren( OntologyTermI term );

    Set<OntologyTermI> getAllParents( OntologyTermI term );

    Set<OntologyTermI> getAllChildren( OntologyTermI term );
}