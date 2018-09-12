
package org.biopax.ols;

/*
 *
 */

import java.util.Collection;

/**
 * @author R. Cote
 * @version $Id: OntologyAccess.java,v 1.8 2006/06/07 09:28:26 rglcote Exp $
 */
public interface Ontology {

    public String getShortOntologyName();
    public String getFullOntologyName();
    public String getDefinition();
    public Collection<Term> getTerms();
    public Collection<Term> getRootTerms();
    public String getVersion();
    public String getQueryURL();
    public String getSourceURL();
    public boolean isFullyLoaded();
    public boolean isUsesImports();
    public java.util.Date getLoadDate();
}









