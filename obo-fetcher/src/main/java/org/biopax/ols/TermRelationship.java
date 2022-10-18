package org.biopax.ols;

/**
 * @author R. Cote
 * @version $Id: TermRelationship.java,v 1.7 2006/03/23 12:34:17 rglcote Exp $
 */
public interface TermRelationship {
    /**
     * <p>Does ...</p>
     *
     * @return
     */
    Term getPredicateTerm();

    /**
     * <p>Does ...</p>
     *
     * @return
     */
    Term getSubjectTerm();

    /**
     * <p>Does ...</p>
     *
     * @return
     */
    Term getObjectTerm();

    /**
     * <p>Does ...</p>
     *
     * @return
     */
    Ontology getParentOntology();

}
