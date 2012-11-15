package org.biopax.uk.ac.ebi.ols.impl;

import uk.ac.ebi.proteomics.common.CommonUtilities;

import java.util.Collection;

import org.biopax.uk.ac.ebi.ols.Annotation;
import org.biopax.uk.ac.ebi.ols.DbXref;
import org.biopax.uk.ac.ebi.ols.Ontology;
import org.biopax.uk.ac.ebi.ols.Term;
import org.biopax.uk.ac.ebi.ols.TermPath;
import org.biopax.uk.ac.ebi.ols.TermRelationship;
import org.biopax.uk.ac.ebi.ols.TermSynonym;


/**
 * @author R. Cote
 * @version $Id: TermBean.java,v 1.2 2006/11/24 13:41:27 rglcote Exp $
 */

public class TermBean implements Term {

    /**
     * <p>Represents ...</p>
     */
    private String name = null;

    /**
     * <p>Represents ...</p>
     */
    private String definition = null;

    /**
     * <p>Represents ...</p>
     */
    private String termPk;

    /**
     * <p>Represents ...</p>
     */
    private boolean obsolete = false;

    /**
     * <p>Represents ...</p>
     */
    private boolean rootTerm = false;

    /**
     * <p>Represents ...</p>
     */
    private boolean leaf = false;

    /**
     * <p>Represents ...</p>
     */
    private boolean instance = false;

    /**
     * <p>Represents ...</p>
     */
    private long parentOntologyId;

    /**
     * <p>Represents ...</p>
     */
    private Ontology parentOntology = null;

    /**
     * <p>Represents ...</p>
     */
    private Collection<TermSynonym> synonyms = null;

    /**
     * <p>Represents ...</p>
     */
    private Collection<TermPath> paths = null;

    /**
     * <p>Represents ...</p>
     */
    private Collection<TermRelationship> relationships = null;

    /**
     * <p>Represents ...</p>
     */
    private Collection<Annotation> annotations = null;

    /**
     * <p>Represents ...</p>
     */
    private Collection<DbXref> xrefs = null;

    /**
     * <p>Represents ...</p>
     */
    private String identifier = null;

    /**
     * <p>Represents ...</p>
     */
    private String namespace = null;

    /**
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * @param _name
     */
    public void setName(final String _name) {
        name = _name;
    }

    /**
     * @return
     */
    public String getDefinition() {
        return definition;
    }

    /**
     * @param _definition
     */
    public void setDefinition(final String _definition) {
        definition = _definition;
    }

    /**
     * @return
     */
    public boolean isObsolete() {
        return obsolete;
    }

    /**
     * @param _obsolete
     */
    public void setObsolete(final boolean _obsolete) {
        obsolete = _obsolete;
    }

    /**
     * @return
     */
    public boolean isRootTerm() {
        return rootTerm;
    }

    /**
     * @param _rootTerm
     */
    public void setRootTerm(final boolean _rootTerm) {
        rootTerm = _rootTerm;
    }

    public boolean isLeaf() {
        return leaf;
    }

    public void setLeaf(final boolean _leaf) {
        leaf = _leaf;
    }

    public boolean isInstance() {
        return instance;
    }

    public void setInstance(final boolean _instance) {
        instance = _instance;
    }

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
    public Collection<TermSynonym> getSynonyms() {
        return synonyms;
    }

    /**
     * @param _synonyms
     */
    public void setSynonyms(final Collection<TermSynonym> _synonyms) {
        synonyms = _synonyms;
    }

    /**
     * @return
     */
    public Collection<TermPath> getPaths() {
        return paths;
    }

    /**
     * @param _paths
     */
    public void setPaths(final Collection<TermPath> _paths) {
        paths = _paths;
    }

    /**
     * @return
     */
    public Collection<TermRelationship> getRelationships() {
        return relationships;
    }

    /**
     * @param _relationshipts
     */
    public void setRelationships(final Collection<TermRelationship> _relationshipts) {
        relationships = _relationshipts;
    }

    /**
     * @return
     */
    public Collection<Annotation> getAnnotations() {
        return annotations;
    }

    /**
     * @param _annotations
     */
    public void setAnnotations(final Collection<Annotation> _annotations) {
        annotations = _annotations;
    }

    /**
     * @return
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * @param _identifier
     */
    public void setIdentifier(final String _identifier) {
        identifier = _identifier;
    }

    /**
     * @return
     */
    public Collection<DbXref> getXrefs() {
        return xrefs;
    }

    /**
     * @param _xrefs
     */
    public void setXrefs(final Collection<DbXref> _xrefs) {
        xrefs = _xrefs;
        if (xrefs != null) {
            for (DbXref xref : xrefs) {
                ((DbXrefBean) xref).setQueryXrefType(DbXrefBean.OJB__TERM_XREF);
            }
        }
    }

    public String toString() {
        return new StringBuffer()
                .append("TermBean{")
                .append("name='").append(name).append('\'')
                .append(", termPk=").append(termPk)
                .append(", identifier='").append(identifier).append('\'')
                .append(", namespace='").append(namespace).append('\'')
                .append('}').toString();
    }

    public boolean equals(Object o) {
        if (o instanceof TermBean) {
            TermBean tb = (TermBean) o;
            if (CommonUtilities.xorNull(identifier, tb.getIdentifier())) {
                return false;
            } else {
                if (identifier != null) {
                    return identifier.equals(tb.getIdentifier());
                } else {
                    // if we're still here, identifier is null in both termbeans
                    return true;
                }
            }
        } else {
            return false;
        }
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getTermPk() {
        return termPk;
    }

    public void setTermPk(String termPk) {
        this.termPk = termPk;
    }
}
