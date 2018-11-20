package org.biopax.validator;

import org.biopax.paxtools.model.level3.ControlledVocabulary;

import java.util.Set;

public interface CvFactory {
  /**
   * Gets a normalized BioPAX Controlled Vocabulary by standard (MIRIAM EBI) URI.
   *
   * @param <T>  BioPAX ControlledVocabulary type or sub-type
   * @param uri  recommended URI, e.g., urn:miriam:obo.go:GO%3A0005654 or http://identifiers.org/obo.go/GO:0005654
   * @param cvClass ControlledVocabulary or sub-class
   * @param xmlBase biopax model xml:base (URI namespace prefix for generated CV objects)
   * @return controlled vocabulary object
   */
  <T extends ControlledVocabulary> T getControlledVocabulary(String uri, Class<T> cvClass, String xmlBase);

  /**
   * Lookup for a CV of given class by ontology (name, synonym, or URI)
   * and accession (ID) or term name/synonym.
   *
   * @param <T> BioPAX ControlledVocabulary type or sub-type
   * @param db  OBO ontology name, synonym, or URI (e.g., "Gene Ontology", "go",
   *            "urn:miriam:obo.go", or "http://identifiers.org/obo.go/")
   * @param id  term's accession number (identifier) or name/synonym
   * @param cvClass ControlledVocabulary or sub-class
   * @param xmlBase biopax model xml:base (URI namespace prefix for generated CV objects)
   * @return the controlled vocabulary or null (when no match found or ambiguous)
   */
  <T extends ControlledVocabulary> T getControlledVocabulary(String db, String id, Class<T> cvClass, String xmlBase);

  //controlled vocabulary terms hierarchy methods

  Set<String> getDirectChildren(String urn);

  Set<String> getDirectParents(String urn);

  Set<String> getAllChildren(String urn);

  Set<String> getAllParents(String urn);

  boolean isChild(String parentUrn, String urn);

}
