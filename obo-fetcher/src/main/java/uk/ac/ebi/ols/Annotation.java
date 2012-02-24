package uk.ac.ebi.ols;

/**
 * @author R. Cote
 * @version $Id: Annotation.java,v 1.10 2006/06/07 15:48:35 rglcote Exp $
 */
public interface Annotation {

    public static final String OBO_COMMENT = "comment";
    public static final String OBO_CONSIDER_REPLACEMENT = "consider replacement";
    public static final String OBO_REPLACED_BY = "replaced by";
    public static final String SUBSET = "subset";

    /**
     * <p>Does ...</p>
     *
     * @return
     */
    public Term getParentTerm();

    /**
     * <p>Does ...</p>
     *
     * @return
     */
    public String getAnnotationType();

    /**
     * <p>Does ...</p>
     *
     * @return
     */
    public String getAnnotationStringValue();
    public Double getAnnotationNumberValue();
    public String getAnnotationCompleteValue();
}









