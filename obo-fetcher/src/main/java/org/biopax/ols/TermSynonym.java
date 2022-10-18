package org.biopax.ols;

import java.util.Collection;

/**
 * @author R. Cote
 * @version $Id: TermSynonym.java,v 1.6 2006/03/23 12:34:17 rglcote Exp $
 */
public interface TermSynonym {
    /**
     * <p>Does ...</p>
     *
     * @return
     */
    String getSynonym();

    /**
     * <p>Does ...</p>
     *
     * @return
     */
    Term getParentTerm();

    /**
     * <p>Does ...</p>
     *
     * @return
     */
    Term getSynonymType();

    /**
     * <p>Does ...</p>
     *
     * @return
     */
    Collection<DbXref> getSynonymXrefs();
}
