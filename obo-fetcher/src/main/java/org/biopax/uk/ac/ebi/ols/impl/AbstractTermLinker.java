package org.biopax.uk.ac.ebi.ols.impl;

import org.biopax.uk.ac.ebi.ols.Ontology;
import org.biopax.uk.ac.ebi.ols.Term;


/**
 * <p>relationship between controlled vocabulary / ontology term
 * <p>we use subject/predicate/object but this could also be thought of as child/relationship-type/parent.
 * <p>the subject/predicate/object naming is better as we can think of the graph as composed of statements.
 * <p>we also treat the relationshiptypes / predicates as controlled terms in themselves; this is quite useful as a lot of systems (eg GO) will soon require ontologies of relationship types (eg subtle differences in the partOf relationship)
 *
 * @author R. Cote
 * @version $Id: AbstractTermLinker.java,v 1.3 2006/11/24 13:41:22 rglcote Exp $
 */
public abstract class AbstractTermLinker {

    /**
     * parent ontology primary key for OJB
     */
    private long parentOntologyId;

    /**
     * parent ontology object for OJB
     */
    private Ontology parentOntology = null;

    /**
     * subejct term primary key for OJB
     */
    private String subjectTermPk;

    /**
     * subejct term object for OJB
     */
    private Term subjectTerm = null;

    /**
     * predicate term primary key for OJB
     */
    private String predicateTermPk;

    /**
     * predicate term object for OJB
     */
    private Term predicateTerm = null;

    /**
     * object term primary key for OJB
     */
    private String objectTermPk;

    /**
     * object term object for OJB
     */
    private Term objectTerm = null;

    /**
     * @return
     */
    public long getParentOntologyId() {
        return parentOntologyId;
    }

    /**
     * @param _parentOntologyId
     */
    public void setParentOntologyId(final long _parentOntologyId) {
        parentOntologyId = _parentOntologyId;
    }

    /**
     * @return
     */
    public Ontology getParentOntology() {
        return parentOntology;
    }

    /**
     * @param _parentOntology
     */
    public void setParentOntology(final Ontology _parentOntology) {
        parentOntology = _parentOntology;
    }

    /**
     * @return
     */
    public Term getSubjectTerm() {
        return subjectTerm;
    }

    /**
     * @param _subjectTerm
     */
    public void setSubjectTerm(final Term _subjectTerm) {
        subjectTerm = _subjectTerm;
    }

    /**
     * @return
     */
    public Term getPredicateTerm() {
        return predicateTerm;
    }

    /**
     * @param _predicateTerm
     */
    public void setPredicateTerm(final Term _predicateTerm) {
        predicateTerm = _predicateTerm;
    }

    /**
     * @return
     */
    public Term getObjectTerm() {
        return objectTerm;
    }

    /**
     * @param _objectTerm
     */
    public void setObjectTerm(final Term _objectTerm) {
        objectTerm = _objectTerm;
    }

    public String toString() {
        return new StringBuffer()
            .append("AbstractTermLinker{")
            .append(" subjectTerm=").append((subjectTerm != null) ? subjectTerm.getIdentifier() : null)
            .append(", predicateTerm=").append((predicateTerm != null) ? predicateTerm.getIdentifier() : null)
            .append(", objectTerm=").append((objectTerm != null) ? objectTerm.getIdentifier() : null)
            .append('}').toString();
    }


    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AbstractTermLinker that = (AbstractTermLinker) o;

        if (parentOntologyId != that.parentOntologyId) return false;
        if (objectTerm != null ? !objectTerm.equals(that.objectTerm) : that.objectTerm != null) return false;
        if (predicateTerm != null ? !predicateTerm.equals(that.predicateTerm) : that.predicateTerm != null)
            return false;
        if (subjectTerm != null ? !subjectTerm.equals(that.subjectTerm) : that.subjectTerm != null) return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = (int) (parentOntologyId ^ (parentOntologyId >>> 32));
        result = 31 * result + (subjectTerm != null ? subjectTerm.hashCode() : 0);
        result = 31 * result + (predicateTerm != null ? predicateTerm.hashCode() : 0);
        result = 31 * result + (objectTerm != null ? objectTerm.hashCode() : 0);
        return result;
    }

    public String getSubjectTermPk() {
        return subjectTermPk;
    }

    public void setSubjectTermPk(String subjectTermPk) {
        this.subjectTermPk = subjectTermPk;
    }

    public String getPredicateTermPk() {
        return predicateTermPk;
    }

    public void setPredicateTermPk(String predicateTermPk) {
        this.predicateTermPk = predicateTermPk;
    }

    public String getObjectTermPk() {
        return objectTermPk;
    }

    public void setObjectTermPk(String objectTermPk) {
        this.objectTermPk = objectTermPk;
    }
}
