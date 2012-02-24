package uk.ac.ebi.ols;

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









