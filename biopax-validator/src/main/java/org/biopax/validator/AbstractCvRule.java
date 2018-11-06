package org.biopax.validator;

import org.biopax.paxtools.controller.PropertyEditor;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.validator.api.CvRestriction;
import org.biopax.validator.api.CvRule;
import org.biopax.validator.api.CvUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.Set;

/**
 * An abstract class for CV terms checks.
 *
 * @param <D> property domain
 * @author rodche
 */
public abstract class AbstractCvRule<D extends BioPAXElement> extends AbstractRule<D> implements CvRule<D> {

  @Autowired
  protected CvUtils ontologyUtils;

  protected final Class<D> domain;
  protected final String property; // helps validate generic ControlledVocabulary instances
  protected final Set<CvRestriction> restrictions;
  private Set<String> validTerms;
  protected PropertyEditor<? super D, ?> editor;

  /**
   * Constructor.
   *
   * @param domain       a BioPAX class for which the CV terms restrictions apply
   * @param property     the name of the BioPAX property to get controlled vocabularies or null
   * @param restrictions a list of beans, each defining names (a subtree of an ontology) that
   *                     is either to include or exclude (when 'not' flag is set) from the valid names set.
   */
  public AbstractCvRule(Class<D> domain, String property, CvRestriction... restrictions) {
    this.domain = domain;
    this.property = property;
    this.restrictions = new HashSet<>(restrictions.length);
    for (CvRestriction c : restrictions) {
      this.restrictions.add(c);
    }
  }

  @PostConstruct
  public void init() {
    if (ontologyUtils != null) {
      setValidTerms(ontologyUtils.getValidTermNames(this));
    } else {
      throw new IllegalStateException("ontologyUtils is NULL!");
    }
  }

  ;


  public boolean canCheck(Object thing) {
    return domain.isInstance(thing);
  }

  /* (non-Javadoc)
   * @see org.biopax.validator.impl.CvRule#getValidTerms()
   */
  public Set<String> getValidTerms() {
    return validTerms;
  }

  /* (non-Javadoc)
   * @see org.biopax.validator.impl.CvRule#setValidTerms(java.util.Set)
   */
  public void setValidTerms(Set<String> validTerms) {
    this.validTerms = validTerms;
  }

  // for unit testing

  /* (non-Javadoc)
   * @see org.biopax.validator.impl.CvRule#getRestrictions()
   */
  public Set<CvRestriction> getRestrictions() {
    return restrictions;
  }

  /* (non-Javadoc)
   * @see org.biopax.validator.impl.CvRule#getDomain()
   */
  public Class<D> getDomain() {
    return domain;
  }

  /* (non-Javadoc)
   * @see org.biopax.validator.impl.CvRule#getProperty()
   */
  public String getProperty() {
    return property;
  }

  /**
   * Gets the internal BiopaxOntologyManager instance
   *
   * @return ontology manager
   */
  public CvUtils getBiopaxOntologyManager() {
    return ontologyUtils;
  }

  /**
   * Gets the corresponding CV property editor.
   * Returns null if either the 'domain' itself is of CV type
   * or the 'property' is null.
   *
   * @return biopax property editor
   */
  public PropertyEditor<? super D, ?> getEditor() {
    return editor;
  }

  /**
   * OntologyAccess IDs used to check this CV rule.
   * These can be extracted from the CV restrictions
   * used to define the rule.
   * (other ontologies are not used).
   *
   * @return ontology IDs, such as e.g. 'MI', 'GO'
   */
  protected Set<String> getOntologyIDs() {
    Set<String> ids = new HashSet<>();
    for (CvRestriction restriction : restrictions)
      ids.add(restriction.getOntologyId());
    return ids;
  }

}
