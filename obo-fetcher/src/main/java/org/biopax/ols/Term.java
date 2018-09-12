package org.biopax.ols;

/*
 *
 */

import java.util.Collection;

/**
 * @author R. Cote
 * @version $Id: Term.java,v 1.13 2006/03/23 12:34:17 rglcote Exp $
 */
public interface Term {
    /**
     * <p>Does ...</p>
     *
     * @return
     */
    public String getDefinition();

    /**
     * <p>Does ...</p>
     *
     * @return
     */
    public String getIdentifier();

    /**
     * <p>Does ...</p>
     *
     * @return
     */
    public boolean isObsolete();

    /**
     * <p>Does ...</p>
     *
     * @return
     */
    public boolean isRootTerm();

    /**
     * <p>Does ...</p>
     *
     * @return
     */
    public boolean isLeaf();

    /**
     * <p>Does ...</p>
     *
     * @return
     */
    public boolean isInstance();

    /**
     * <p>Does ...</p>
     *
     * @return
     */
    public String getName();

    /**
     * <p>Does ...</p>
     *
     * @return
     */
    public Ontology getParentOntology();

    /**
     * <p>Does ...</p>
     *
     * @return
     */
    public Collection<TermSynonym> getSynonyms();

    /**
     * <p>Does ...</p>
     *
     * @return
     */
    public Collection<TermRelationship> getRelationships();

    /**
     * <p>Does ...</p>
     *
     * @return
     */
    public Collection<TermPath> getPaths();

    /**
     * <p>Does ...</p>
     *
     * @return
     */
    public String getNamespace();

    /**
     * <p>Does ...</p>
     *
     * @return
     */
    public String getTermPk();

    /**
     * <p>Does ...</p>
     *
     * @return
     */
    public Collection<Annotation> getAnnotations();

    /**
     * <p>Does ...</p>
     *
     * @return
     */
    public Collection<DbXref> getXrefs();


}









