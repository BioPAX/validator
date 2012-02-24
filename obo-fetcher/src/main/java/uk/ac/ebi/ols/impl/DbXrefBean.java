package uk.ac.ebi.ols.impl;


import uk.ac.ebi.ols.DbXref;
import uk.ac.ebi.proteomics.common.CommonUtilities;

/**
 * <p></p>
 *
 * @author R. Cote
 * @version $Id: DbXrefBean.java,v 1.1 2006/03/23 12:32:41 rglcote Exp $
 */
public class DbXrefBean implements DbXref {

    public static final long OJB__TERM_XREF = 0;
    public static final long OJB__SYNONYM_XREF = 1;

    private String parentObjectPk;
    private long dbXrefId;
    private String dbName = null;
    private String accession = null;
    private String description = null;
    private int xrefType;
    private long queryXrefType;

    public void setQueryXrefType(long queryXrefType) {
        this.queryXrefType = queryXrefType;
    }

    /**
     * @return
     */
    public long getDbXrefId() {
        return dbXrefId;
    }

    /**
     * @param _dbXrefId
     */
    public void setDbXrefId(final long _dbXrefId) {
        dbXrefId = _dbXrefId;
    }

    /**
     * @return
     */
    public String getDbName() {
        return dbName;
    }

    /**
     * @param _dbName
     */
    public void setDbName(final String _dbName) {
        dbName = _dbName;
    }

    /**
     * @return
     */
    public String getAccession() {
        return accession;
    }

    /**
     * @param _accession
     */
    public void setAccession(final String _accession) {
        accession = _accession;
    }

    /**
     * @return
     */
    public int getXrefType() {
        return xrefType;
    }

    /**
     * @param _version
     */
    public void setXrefType(final int _version) {
        xrefType = _version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getParentObjectPk() {
        return parentObjectPk;
    }

    public void setParentObjectPk(String parentObjectPk) {
        this.parentObjectPk = parentObjectPk;
    }

    public String toString() {
        return "DbName: " + dbName + " Acc: " + accession + " type: " + xrefType;
    }

    public boolean equals(Object o) {
        if (o instanceof DbXrefBean) {
            if (o != null) {
                DbXrefBean dx = (DbXrefBean) o;
                if (CommonUtilities.xorNull(dbName, dx.getDbName()) ||
                        CommonUtilities.xorNull(accession, dx.getAccession())) {
                    return false;
                } else {
                    boolean retval = true;

                    //if still here, dbName is either null or not null in both DbXrefBeans
                    //if not null, compare for equality
                    if (dbName != null)
                        retval = retval && dbName.equals(dx.getDbName());

                    //same for dbXrefs
                    if (accession != null)
                        retval = retval && accession.equals(dx.getAccession());

                    return retval;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

 }
