package org.biopax.ols;

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
    String getDefinition();

    /**
     * <p>Does ...</p>
     *
     * @return
     */
    String getIdentifier();

    /**
     * <p>Does ...</p>
     *
     * @return
     */
    boolean isObsolete();

    /**
     * <p>Does ...</p>
     *
     * @return
     */
    boolean isRootTerm();

    /**
     * <p>Does ...</p>
     *
     * @return
     */
    boolean isLeaf();

    /**
     * <p>Does ...</p>
     *
     * @return
     */
    boolean isInstance();

    /**
     * <p>Does ...</p>
     *
     * @return
     */
    String getName();

    /**
     * <p>Does ...</p>
     *
     * @return
     */
    Ontology getParentOntology();

    /**
     * <p>Does ...</p>
     *
     * @return
     */
    Collection<TermSynonym> getSynonyms();

    /**
     * <p>Does ...</p>
     *
     * @return
     */
    Collection<TermRelationship> getRelationships();

    /**
     * <p>Does ...</p>
     *
     * @return
     */
    Collection<TermPath> getPaths();

    /**
     * <p>Does ...</p>
     *
     * @return
     */
    String getNamespace();

    /**
     * <p>Does ...</p>
     *
     * @return
     */
    String getTermPk();

    /**
     * <p>Does ...</p>
     *
     * @return
     */
    Collection<Annotation> getAnnotations();

    /**
     * <p>Does ...</p>
     *
     * @return
     */
    Collection<DbXref> getXrefs();
}
