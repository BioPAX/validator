package org.biopax.ols.impl;

import org.biopax.ols.DbXref;

import java.io.Serializable;

public class DbXrefBean implements DbXref, Serializable {

    private static final long serialVersionUID = 1L;

    private String parentObjectPk;
    private long dbXrefId;
    private String dbName = null;
    private String accession = null;
    private String description = null;
    private int xrefType;

    public long getDbXrefId() {
        return dbXrefId;
    }

    public void setDbXrefId(final long _dbXrefId) {
        dbXrefId = _dbXrefId;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(final String _dbName) {
        dbName = _dbName;
    }

    public String getAccession() {
        return accession;
    }

    public void setAccession(final String _accession) {
        accession = _accession;
    }

    public int getXrefType() {
        return xrefType;
    }

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

    @Override
    public boolean equals(Object o) {
        if (o instanceof DbXrefBean) {
            if (o != null) {
                DbXrefBean dx = (DbXrefBean) o;
                if (xorNull(dbName, dx.getDbName()) ||
                        xorNull(accession, dx.getAccession())) {
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

    private boolean xorNull(Object a, Object b) {
        return (a == null) ? (b != null) : (b == null) ;
    }
}
