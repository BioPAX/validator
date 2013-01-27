package org.biopax.ols.impl;

/*
 * #%L
 * Ontologies Access
 * %%
 * Copyright (C) 2008 - 2013 University of Toronto (baderlab.org) and Memorial Sloan-Kettering Cancer Center (cbio.mskcc.org)
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import org.biopax.ols.TermPath;

/**
 * @author R. Cote
 * @version $Id: TermPathBean.java,v 1.1 2006/03/23 12:32:41 rglcote Exp $
 */
public class TermPathBean extends AbstractTermLinker implements TermPath {

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
