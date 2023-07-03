package org.biopax.validator;

import org.biopax.paxtools.model.level3.ControlledVocabulary;

import java.util.Set;

public interface CvFactory {
  /**
   * Gets a normalized BioPAX Controlled Vocabulary by normalized URI.
   *
   * @param <T>  BioPAX ControlledVocabulary type or subtype
   * @param uri  a CV URI like urn:miriam:go:GO%3A0005654 (obsolete), identifiers.org/GO:0005654, bioregistry.io/go:0005654 (preferred)
   * @param cvClass ControlledVocabulary or subclass
   * @param xmlBase biopax model xml:base (URI namespace prefix for generated CV objects)
   * @return controlled vocabulary object
   */
  <T extends ControlledVocabulary> T getControlledVocabulary(String uri, Class<T> cvClass, String xmlBase);

  Set<String> getDirectChildren(String urn);

  Set<String> getDirectParents(String urn);

  Set<String> getAllChildren(String urn);

  Set<String> getAllParents(String urn);

  boolean isChild(String parentUrn, String urn);

}
