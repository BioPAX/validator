package org.biopax.ols;

/**
 * @author R. Cote
 * @version $Id: Annotation.java,v 1.10 2006/06/07 15:48:35 rglcote Exp $
 */
public interface Annotation {

    String OBO_COMMENT = "comment";
    String OBO_CONSIDER_REPLACEMENT = "consider replacement";
    String OBO_REPLACED_BY = "replaced by";
    String SUBSET = "subset";

    /**
     * <p>Does ...</p>
     *
     * @return
     */
    Term getParentTerm();

    /**
     * <p>Does ...</p>
     *
     * @return
     */
    String getAnnotationType();

    /**
     * <p>Does ...</p>
     *
     * @return
     */
    String getAnnotationStringValue();
    Double getAnnotationNumberValue();
    String getAnnotationCompleteValue();
}
