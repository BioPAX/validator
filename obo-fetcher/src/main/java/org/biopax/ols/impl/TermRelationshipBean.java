package org.biopax.ols.impl;

/*
 *
 */

import org.biopax.ols.TermRelationship;

import java.io.Serializable;

/**
 * @author R. Cote
 * @version $Id: TermRelationshipBean.java,v 1.2 2006/11/24 13:41:27 rglcote Exp $
 */
public class TermRelationshipBean extends AbstractTermLinker implements TermRelationship, Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * <p>Represents ...</p>
     */
    private long termRelationshipId;

    /**
     * @return
     */
    public long getTermRelationshipId() {
        return termRelationshipId;
    }

    /**
     *
     *
     *
     *
     * @param _termRelationshipId
     */
    public void setTermRelationshipId(final long _termRelationshipId) {
        termRelationshipId = _termRelationshipId;
    }



 }
