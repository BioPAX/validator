
package org.biopax.ols;

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









