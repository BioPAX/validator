package org.biopax.ols.impl;

/*
 *
 */

import org.biopax.ols.TermPath;

import java.io.Serializable;

/**
 * @author R. Cote
 * @version $Id: TermPathBean.java,v 1.1 2006/03/23 12:32:41 rglcote Exp $
 */
public class TermPathBean extends AbstractTermLinker implements TermPath, Serializable {

    private static final long serialVersionUID = 1L;

    private long relationshipTypeId;

    /**
     * <p>Represents ...</p>
     */
    private long termPathId;

    /**
     * <p>Represents ...</p>
     */
    private int distance;

    /**
     * @return
     */
    public long getTermPathId() {
        return termPathId;
    }

    /**
     * @param _termPathId
     */
    public void setTermPathId(final long _termPathId) {
        termPathId = _termPathId;
    }

    /**
     * @return
     */
    public int getDistance() {
        return distance;
    }

    /**
     * @param _distance
     */
    public void setDistance(final int _distance) {
        distance = _distance;
    }

    public long getRelationshipTypeId() {
        return relationshipTypeId;
    }

    public void setRelationshipTypeId(long relationshipTypeId) {
        this.relationshipTypeId = relationshipTypeId;
    }
}
