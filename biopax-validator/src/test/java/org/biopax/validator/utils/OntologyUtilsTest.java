package org.biopax.validator.utils;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import org.biopax.paxtools.model.level3.*;
import org.biopax.validator.CvFactory;
import org.biopax.validator.XrefUtils;
import org.biopax.validator.api.CvUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;


/**
 * Unit test OntologyUtils' interfaces:  CvUtils, CvFactory, XrefUtils
 */
public class OntologyUtilsTest {

  private static CvUtils cvUtils;
  private static CvFactory cvFactory;
  private static XrefUtils xrefUtils;

  @BeforeAll
  public static void init() throws IOException {
    OntologyUtils utils = new OntologyUtils();
    utils.setOntologyConfig(PropertiesLoaderUtils.loadProperties(new ClassPathResource("test-obo.properties")));
    utils.init();
    cvUtils = utils;
    cvFactory = utils;
    xrefUtils = utils;
  }

  @Test
  public void ontologyLoading() {
    Collection<String> ontologyIDs = cvUtils.getOntologyManager().getOntologyIDs();
    Assertions.assertTrue(ontologyIDs.contains("GO"));
    Assertions.assertEquals("gene ontology", cvUtils
      .getOntologyManager().getOntology("GO").getName().toLowerCase());
    Assertions.assertTrue(ontologyIDs.contains("MOD"));
  }

  @Test
  public void testGetDirectChildren() {
    Set<String> dc = cvFactory.getDirectChildren("urn:miriam:go:GO%3A0005654");
    Assertions.assertFalse(dc.isEmpty());
    Assertions.assertTrue(dc.contains("bioregistry.io/go:0044451"));
  }

  @Test
  public void testGetAllChildren() {
    Set<String> dc = cvFactory.getAllChildren("identifiers.org/GO:0005654");
    Assertions.assertFalse(dc.isEmpty());
    Assertions.assertTrue(dc.contains("bioregistry.io/go:0044451"));
    Assertions.assertTrue(dc.contains("bioregistry.io/go:0071821"));
    Assertions.assertTrue(dc.contains("bioregistry.io/go:0070847"));
  }

  @Test
  public void testGetDirectParents() {
    Set<String> dc = cvFactory.getDirectParents("urn:miriam:go:GO%3A0005654");
    Assertions.assertFalse(dc.isEmpty());
    Assertions.assertTrue(dc.contains("bioregistry.io/go:0031981"));
  }

  @Test
  public void testGetAllParents() {
    Set<String> dc = cvFactory.getAllParents("identifiers.org/go/GO:0005654");
    Assertions.assertFalse(dc.isEmpty());
    Assertions.assertTrue(dc.contains("bioregistry.io/go:0031981"));
    Assertions.assertTrue(dc.contains("bioregistry.io/go:0044428"));
    Assertions.assertTrue(dc.contains("bioregistry.io/go:0044422"));
  }

  @Test // using correct ID(s)
  public void testGetObject() {
    CellularLocationVocabulary cv = cvFactory.getControlledVocabulary(
            "urn:miriam:go:GO%3A0005737", CellularLocationVocabulary.class,"");
    Assertions.assertNotNull(cv);
    cv = cvFactory.getControlledVocabulary( //using a deprecated URL
      "identifiers.org/go/GO:0005737",CellularLocationVocabulary.class,"");
    Assertions.assertNotNull(cv);
    cv = cvFactory.getControlledVocabulary( //using a deprecated URN (obo.go)
        "urn:miriam:obo.go:GO%3A0005737", CellularLocationVocabulary.class,"");
    Assertions.assertNotNull(cv);
    //same
    cv = cvFactory.getControlledVocabulary(
      "identifiers.org/GO:0005737",CellularLocationVocabulary.class,"");
    Assertions.assertNotNull(cv);
  }

  @Test // using bad ID (with 'X' in it)
  public void testGetObject2() {
    CellularLocationVocabulary cv = cvFactory.getControlledVocabulary(
      "urn:miriam:go:GO%3A0005737X",CellularLocationVocabulary.class,"");
    Assertions.assertNull(cv);
  }

  @Test
  public void testEscapeChars() {
    ControlledVocabulary cv = cvFactory.getControlledVocabulary(
      "bioregistry.io/mod:00048",SequenceModificationVocabulary.class,"");
    Assertions.assertNotNull(cv);
    Assertions.assertTrue(cv.getTerm().contains("O4'-phospho-L-tyrosine")); // apostrophe
  }

}
