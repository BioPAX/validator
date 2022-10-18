package org.biopax.ols;

/**
 * @author R. Cote
 * @version $Id: DbXref.java,v 1.6 2006/03/23 12:34:17 rglcote Exp $
 */
public interface DbXref {
    // from definition in OBOEdit codebase
    // of dbxref object
    int OBO_DBXREF_UNKNOWN = -1;
    int OBO_DBXREF_ANATOMICAL = 0;
    int OBO_DBXREF_SYNONYM = 1;
    int OBO_DBXREF_DEFINITION = 2;
    int OBO_DBXREF_ANALOG = 3;

    String OBO_DBXREF_UNKNOWN_STRING = "xref_unknown";
    String OBO_DBXREF_ANATOMICAL_STRING = "xref_anatomical";
    String OBO_DBXREF_SYNONYM_STRING = "xref_related_synonym";
    String OBO_DBXREF_DEFINITION_STRING = "xref_definition";
    String OBO_DBXREF_ANALOG_STRING = "xref_analog";

    String getDbName();

    String getAccession();

    int getXrefType();

    String getDescription();
 }
