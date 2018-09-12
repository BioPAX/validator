package org.biopax.ols;

/*
 *
 */

/**
 * @author R. Cote
 * @version $Id: TermPath.java,v 1.5 2006/03/23 12:34:17 rglcote Exp $
 */
public interface TermPath extends TermRelationship {
    /**
     * <p>Does ...</p>
     *
     * @return
     */
    public int getDistance();

    public long getRelationshipTypeId();
}









