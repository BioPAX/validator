package org.biopax.ols.impl;

import org.biopax.ols.Annotation;
import org.biopax.ols.Term;

import java.io.Serializable;

/**
 * <p>Implementation of Annotation interface</p>
 *
 * @author R. Cote
 * @version $Id: AnnotationBean.java,v 1.4 2008/05/20 16:40:00 rglcote Exp $
 */
public class AnnotationBean implements Annotation, Serializable {

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
    private String annotationType = null;

    /**
     * <p>Represents ...</p>
     */
    private String annotationStringValue = null;
    private Double annotationNumberValue = null;

    /**
     * <p>Represents ...</p>
     */
    private long annotationId;

    public String getParentTermPk() {
        return parentTermPk;
    }

    public void setParentTermPk(String parentTermPk) {
        this.parentTermPk = parentTermPk;
    }

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
    public String getAnnotationType() {
        return annotationType;
    }

    /**
     * @param _annotationName
     */
    public void setAnnotationType(final String _annotationName) {
        annotationType = _annotationName;
    }

    /**
     * @return
     */
    public long getAnnotationId() {
        return annotationId;
    }

    /**
     * @param _annotationId
     */
    public void setAnnotationId(final long _annotationId) {
        annotationId = _annotationId;
    }

    public String getAnnotationStringValue() {
        return annotationStringValue;
    }

    public void setAnnotationStringValue(String annotationStringValue) {
        this.annotationStringValue = annotationStringValue;
    }

    public Double getAnnotationNumberValue() {
        return annotationNumberValue;
    }

    /**
     * returns "annotationStringValue, annotationNumberValue". If both are null, returns null. If either is not not null
     * there will be no comma.
     */
    public String getAnnotationCompleteValue() {
        //start building string - this can be null!
        String retval = getAnnotationStringValue();
        //if we have a number value
        if (getAnnotationNumberValue() != null){
            if (retval == null){
                //if we don't have a string value, set it as data
                retval = getAnnotationNumberValue().toString();
            } else {
                //otherwise, append it to previous string value
                retval = new StringBuffer().
                        append(retval).
                        append(',').
                        append(getAnnotationNumberValue())
                        .toString();
            }
        }

        return retval;
    }

    public void setAnnotationNumberValue(Double annotationNumberValue) {
        this.annotationNumberValue = annotationNumberValue;
    }


    public String toString() {
        return "AnnotationBean{" +
                "annotationType='" + annotationType + '\'' +
                ", annotationStringValue='" + annotationStringValue + '\'' +
                ", annotationNumberValue=" + annotationNumberValue +
                '}';
    }

    public void setAnnotationDoubleValue(String str) {
        try {
            if (str != null && !str.equals("none")){
                annotationNumberValue = Double.parseDouble(str);
            }
        } catch (NumberFormatException nfe){
            throw new RuntimeException("AnnotationBean.setAnnotationDoubleValue could not parse string: " + str, nfe);
        }
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final AnnotationBean that = (AnnotationBean) o;

        if (annotationId != that.annotationId) return false;
        if (parentTermPk != that.parentTermPk) return false;
        if (annotationType != null ? !annotationType.equals(that.annotationType) : that.annotationType != null)
            return false;
        if (annotationNumberValue != null ? !annotationNumberValue.equals(that.annotationNumberValue) : that.annotationNumberValue != null)
            return false;
        if (annotationStringValue != null ? !annotationStringValue.equals(that.annotationStringValue) : that.annotationStringValue != null)
            return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = (parentTermPk != null ? parentTermPk.hashCode() : 0);
        result = 31 * result + (parentTerm != null ? parentTerm.hashCode() : 0);
        result = 31 * result + (annotationType != null ? annotationType.hashCode() : 0);
        result = 31 * result + (annotationStringValue != null ? annotationStringValue.hashCode() : 0);
        result = 31 * result + (annotationNumberValue != null ? annotationNumberValue.hashCode() : 0);
        result = 31 * result + (int) (annotationId ^ (annotationId >>> 32));
        return result;
    }
}
