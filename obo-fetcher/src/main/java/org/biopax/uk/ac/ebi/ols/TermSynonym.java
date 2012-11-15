package org.biopax.uk.ac.ebi.ols;

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
    public String getSynonym();

    /**
     * <p>Does ...</p>
     *
     * @return
     */
    public Term getParentTerm();

    /**
     * <p>Does ...</p>
     *
     * @return
     */
    public Term getSynonymType();

    /**
     * <p>Does ...</p>
     *
     * @return
     */
    public Collection<DbXref> getSynonymXrefs();
}









