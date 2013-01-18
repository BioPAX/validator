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

/**
 * @author R. Cote
 * @version $Id: DbXref.java,v 1.6 2006/03/23 12:34:17 rglcote Exp $
 */
public interface DbXref {

    // from definition in OBOEdit codebase
    // of dbxref object
    public final static int OBO_DBXREF_UNKNOWN = -1;
    public final static int OBO_DBXREF_ANATOMICAL = 0;
    public final static int OBO_DBXREF_SYNONYM = 1;
    public final static int OBO_DBXREF_DEFINITION = 2;
    public final static int OBO_DBXREF_ANALOG = 3;

    public final static String OBO_DBXREF_UNKNOWN_STRING = "xref_unknown";
    public final static String OBO_DBXREF_ANATOMICAL_STRING = "xref_anatomical";
    public final static String OBO_DBXREF_SYNONYM_STRING = "xref_related_synonym";
    public final static String OBO_DBXREF_DEFINITION_STRING = "xref_definition";
    public final static String OBO_DBXREF_ANALOG_STRING = "xref_analog";

    public String getDbName();

    public String getAccession();

    public int getXrefType();

    public String getDescription();

 }









