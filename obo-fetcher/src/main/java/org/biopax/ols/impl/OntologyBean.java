package org.biopax.ols.impl;

import org.biopax.ols.Ontology;
import org.biopax.ols.Term;
import org.biopax.ols.TermPath;
import org.biopax.ols.TermRelationship;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

public class OntologyBean implements Ontology, Serializable {

    private static final long serialVersionUID = 1L;

    private long ontologyId;

    private String shortOntologyName = null;

    private String fullOntologyName = null;

    private String definition = null;

    private String queryURL = null;

    private String sourceURL = null;

    private Collection<Term> terms = null;

    private Collection<Term> rootTerms = null;

    private Collection<TermRelationship> termRelationships = null;

    private Collection<TermPath> termPaths = null;

    private java.sql.Date loadDate = null;

    private String version = null;

    private boolean fullyLoaded = false;

    private boolean usesImports = false;

    public long getOntologyId() {
        return ontologyId;
    }

    public void setOntologyId(final long _ontologyId) {
        ontologyId = _ontologyId;
    }

    public String getShortOntologyName() {
        return shortOntologyName;
    }

    public void setShortOntologyName(final String _ontologyName) {
        shortOntologyName = _ontologyName;
    }

    public String getFullOntologyName() {
        return fullOntologyName;
    }

    public void setFullOntologyName(final String _ontologyName) {
        fullOntologyName = _ontologyName;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(final String _definition) {
        definition = _definition;
    }

    public Collection<Term> getTerms() {
        return terms;
    }

    public void setTerms(final Collection<Term> _terms) {
        terms = _terms;
    }

    public Collection<Term> getRootTerms() {
        if (rootTerms == null) {
            rootTerms = new ArrayList<>();
            if (terms != null) {
                for (Term term : terms) {
                    if (term.isRootTerm()) {
                        rootTerms.add(term);
                    }
                }
            }
        }
        return rootTerms;
    }

    public void setRootTerms(Collection<Term> rootTerms) {
        this.rootTerms = rootTerms;
    }

    public Collection<TermRelationship> getTermRelationships() {
        return termRelationships;
    }

    public void setTermRelationships(final Collection<TermRelationship> _termRelationships) {
        termRelationships = _termRelationships;
    }

    public Collection<TermPath> getTermPaths() {
        return termPaths;
    }

    public void setTermPaths(final Collection<TermPath> _termPaths) {
        termPaths = _termPaths;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(final String _version) {
        version = _version;
    }

    public java.util.Date getLoadDate() {
        return loadDate;
    }

    public void setLoadDate(final java.sql.Date _loadDate) {
        loadDate = _loadDate;
    }

    public void setLoadData(final java.util.Date _loadDate) {
        loadDate = new java.sql.Date(_loadDate.getTime());
    }

    public boolean getFullyLoaded() {
        return fullyLoaded;
    }

    public void setFullyLoaded(boolean fullyLoaded) {
        this.fullyLoaded = fullyLoaded;
    }

    public boolean isFullyLoaded() {
        return fullyLoaded;
    }

    public String getQueryURL() {
        return queryURL;
    }

    public void setQueryURL(String queryURL) {
        this.queryURL = queryURL;
    }

    public String getSourceURL() {
        return sourceURL;
    }

    public void setSourceURL(String sourceURL) {
        this.sourceURL = sourceURL;
    }

    public boolean isUsesImports() {
        return usesImports;
    }

    public void setUsesImports(boolean usesImports) {
        this.usesImports = usesImports;
    }

    public String toString() {
        return new StringBuilder().append("OntologyBean{").append("ontologyId=").append(ontologyId).append(", shortOntologyName='").append(shortOntologyName).append('\'').append(", fullOntologyName='").append(fullOntologyName).append('\'').append(", definition='").append(definition).append('\'').append(", queryURL='").append(queryURL).append('\'').append(", sourceURL='").append(sourceURL).append('\'').append(", terms=").append(terms).append(", rootTerms=").append(rootTerms).append(", termRelationships=").append(termRelationships).append(", termPaths=").append(termPaths).append(", loadDate=").append(loadDate).append(", version='").append(version).append('\'').append(", fullyLoaded=").append(fullyLoaded).append(", usesImports=").append(usesImports).append('}').toString();
    }
}

