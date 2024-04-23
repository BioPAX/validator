package org.biopax.validator.utils;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import org.biopax.paxtools.model.level3.*;
import org.biopax.validator.CvFactory;
import org.biopax.validator.XrefUtils;
import org.biopax.validator.api.CvUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.platform.commons.util.CollectionUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import static org.junit.jupiter.api.Assertions.*;

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
    assertAll(
        () -> assertTrue(ontologyIDs.contains("GO")),
        () -> assertEquals("gene ontology", cvUtils.getOntologyManager()
            .getOntology("GO").getName().toLowerCase()),
        () -> assertTrue(ontologyIDs.contains("MOD"))
    );
  }

  @Test
  public void getDirectChildren() {
    Set<String> dc = cvFactory.getDirectChildren("urn:miriam:go:GO%3A0005654");
    assertAll(
        () -> assertFalse(dc.isEmpty()),
        () -> assertTrue(dc.contains("http://bioregistry.io/go:0044451"))
    );
  }

  @Test
  public void getAllChildren() {
    Set<String> dc = cvFactory.getAllChildren("identifiers.org/GO:0005654");
    assertAll(
        () -> assertFalse(dc.isEmpty()),
        () -> assertTrue(dc.contains("http://bioregistry.io/go:0044451")),
        () -> assertTrue(dc.contains("http://bioregistry.io/go:0071821")),
        () -> assertTrue(dc.contains("http://bioregistry.io/go:0070847"))
    );
  }

  @Test
  public void getDirectParents() {
    Set<String> dc = cvFactory.getDirectParents("urn:miriam:go:GO%3A0005654");
    assertAll(
        () -> assertFalse(dc.isEmpty()),
        () -> assertTrue(dc.contains("http://bioregistry.io/go:0031981"))
    );
  }

  @Test
  public void getAllParents() {
    Set<String> dc = cvFactory.getAllParents("identifiers.org/go/GO:0005654");
    assertAll(
        () -> assertFalse(dc.isEmpty()),
        () -> assertTrue(dc.contains("http://bioregistry.io/go:0031981")),
        () -> assertTrue(dc.contains("http://bioregistry.io/go:0044428")),
        () -> assertTrue(dc.contains("http://bioregistry.io/go:0044422"))
    );
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "urn:miriam:go:GO%3A0005737",
      "identifiers.org/go/GO:0005737",
      "urn:miriam:obo.go:GO%3A0005737",
      "https://identifiers.org/GO:0005737",
      "http://bioregistry.io/go:0005737"
  })
  public void buildCellularLocationVocabularyWhenIdOk(String uri) {
    CellularLocationVocabulary cv = cvFactory.getControlledVocabulary(uri
            , CellularLocationVocabulary.class,"");
    assertAll(
        () -> assertTrue(cv instanceof CellularLocationVocabulary),
        () -> assertEquals("cytoplasm", CollectionUtils.getOnlyElement(cv.getTerm())),
        () -> assertTrue(cv.getComment()!=null && cv.getComment().isEmpty())
    );
  }

  @Test // using bad ID (with 'X' in it)
  public void getObject2() {
    CellularLocationVocabulary cv = cvFactory.getControlledVocabulary(
      "urn:miriam:go:GO%3A0005737X",CellularLocationVocabulary.class,"");
    assertNull(cv);
  }

  @Test
  public void escapeChars() {
    ControlledVocabulary cv = cvFactory.getControlledVocabulary(
      "http://bioregistry.io/mod:00048",SequenceModificationVocabulary.class,"");
    assertAll(
        () -> assertTrue(cv instanceof SequenceModificationVocabulary),
        () -> assertTrue(cv.getTerm().contains("O4'-phospho-L-tyrosine")) // apostrophe
    );
  }

}
