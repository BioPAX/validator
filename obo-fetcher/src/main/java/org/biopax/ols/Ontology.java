
package org.biopax.ols;

import java.util.Collection;

/**
 * @author R. Cote
 * @version $Id: OntologyAccess.java,v 1.8 2006/06/07 09:28:26 rglcote Exp $
 */
public interface Ontology {
    String getShortOntologyName();
    String getFullOntologyName();
    String getDefinition();
    Collection<Term> getTerms();
    Collection<Term> getRootTerms();
    String getVersion();
    String getQueryURL();
    String getSourceURL();
    boolean isFullyLoaded();
    boolean isUsesImports();
    java.util.Date getLoadDate();
}
