package org.biopax.ols.impl;

import java.io.Serializable;
import java.util.Collection;

import org.biopax.ols.DbXref;
import org.biopax.ols.Term;
import org.biopax.ols.TermSynonym;

/**
 * @author R. Cote
 * @version $Id: TermSynonymBean.java,v 1.1 2006/03/23 12:32:41 rglcote Exp $
 */
public class TermSynonymBean implements TermSynonym, Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * <p>Represents ...</p>
     */
    private String parentTermPk;

    /**
     * <p>Represents ...</p>
     */
    private Term parentTerm = null;

    /**
     * <p>Represents ...</p>
     */
    private String synonym = null;

    /**
     * <p>Represents ...</p>
     */
    private String synonymPk;

    /**
     * <p>Represents ...</p>
     */
    private String synonymTypePk = null;

    /**
     * <p>Represents ...</p>
     */
    private Term synonymType = null;

    /**
     * <p>Represents ...</p>
     */
    private Collection<DbXref> synonymXrefs = null;

    /**
     * @return
     */
    public Term getParentTerm() {
        return parentTerm;
    }

    /**
     * @param _parentTerm
     */
    public void setParentTerm(final Term _parentTerm) {
        parentTerm = _parentTerm;
    }

    /**
     * @return
     */
    public String getSynonym() {
        return synonym;
    }

    /**
     * @param _synonym
     */
    public void setSynonym(final String _synonym) {
        synonym = _synonym;
    }

    public String getSynonymPk() {
        return synonymPk;
    }

    public void setSynonymPk(String synonymPk) {
        this.synonymPk = synonymPk;
    }

    public String getSynonymTypePk() {
        return synonymTypePk;
    }

    public void setSynonymTypePk(String synonymTypePk) {
        this.synonymTypePk = synonymTypePk;
    }

    /**
     * @return
     */
    public Term getSynonymType() {
        return synonymType;
    }

    /**
     * @param _synonymType
     */
    public void setSynonymType(final Term _synonymType) {
        synonymType = _synonymType;
    }

    public Collection<DbXref> getSynonymXrefs() {
        return synonymXrefs;
    }

    public void setSynonymXrefs(Collection<DbXref> synonymXrefs) {
        this.synonymXrefs = synonymXrefs;
    }

    public String toString() {
        String syntype;
        if (synonymType != null)
            syntype = synonymType.getName();
        else
            syntype = "unknown";
        return "[Synonym]: " + synonym + "(" + syntype  + ")";
    }

    public String getParentTermPk() {
        return parentTermPk;
    }

    public void setParentTermPk(String parentTermPk) {
        this.parentTermPk = parentTermPk;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TermSynonymBean)) return false;

        TermSynonymBean that = (TermSynonymBean) o;

        if (parentTermPk != null ? !parentTermPk.equals(that.parentTermPk) : that.parentTermPk != null) return false;
        if (synonym != null ? !synonym.equals(that.synonym) : that.synonym != null) return false;
        if (synonymPk != null ? !synonymPk.equals(that.synonymPk) : that.synonymPk != null) return false;
        if (synonymTypePk != null ? !synonymTypePk.equals(that.synonymTypePk) : that.synonymTypePk != null)
            return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = (parentTermPk != null ? parentTermPk.hashCode() : 0);
        result = 31 * result + (synonym != null ? synonym.hashCode() : 0);
        result = 31 * result + (synonymPk != null ? synonymPk.hashCode() : 0);
        result = 31 * result + (synonymTypePk != null ? synonymTypePk.hashCode() : 0);
        return result;
    }
}