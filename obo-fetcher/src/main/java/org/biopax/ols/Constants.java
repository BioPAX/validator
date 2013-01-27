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
import java.util.HashSet;

/**
 * User: Richard Cote
 * Date: 21-Jul-2005
 * Time: 14:38:16
 *
 * @author Richard Cote
 * @version $Id: Constants.java,v 1.15 2008/02/12 16:47:13 rglcote Exp $
 */
public class Constants {

    public static final String OLS_CONFIG_FILE = "ols-config.properties";
    public static final String OLS_DBALIAS = "OLS_MAIN";
    public static final String OLS_FAILOVER_DBALIAS = "OLS_FAILOVER";


    public static final int IS_A_RELATION_TYPE_ID = 1;
    public static final int PART_OF_RELATION_TYPE_ID = 2;
    public static final int DEVELOPS_FROM_RELATION_TYPE_ID = 3;
    public static final int OTHER_RELATION_TYPE_ID = 4;
    public static final int NO_DISTANCE_CONSTRAINT = -1;
    public static final String DEFINITION = "definition";

    public static final String IS_A_RELATION_TYPE = "is_a";
    public static final String PART_OF_RELATION_TYPE = "part_of";
    public static final String DEVELOPS_FROM_RELATION_TYPE = "develops_from";
    public static final String ALT_ID_SYNONYM_TYPE = "alt_id";
    public static final String EXACT_SYNONYM_TYPE = "exact";
    public static final String NARROW_SYNONYM_TYPE = "narrow";
    public static final String BROAD_SYNONYM_TYPE = "broad";
    public static final String RELATED_SYNONYM_TYPE = "related";
    public static final String DEFAULT_SYNONYM_TYPE = "synonym";
    public static final String ONTOLOGY_NAME_ATTRIBUTE = "loaded_ontology";
    public static final String PREFERRED_NAME = "preferred name";

    public static final int RETURN_VALUE_OK = 0;
    public static final int RETURN_VALUE_OK_NO_ONTOLOGIES_LOADED = 1;
    public static final int RETURN_VALUE_ERROR = 2;
    public static final int RETURN_VALUE_CVS_ERROR = 3;

    /*
        These are relationship types that need to be ignored because they
        will break the ontology browser and the dot executable
     */
    public static final Collection RELATIONSHIPS_TO_IGNORE = new HashSet();
    static {
        RELATIONSHIPS_TO_IGNORE.add("participates_in");
        RELATIONSHIPS_TO_IGNORE.add("disjoint_from");
    }
}
