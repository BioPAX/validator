package org.biopax.validator.web.service;

import org.biopax.validator.web.dto.Clue;
import org.biopax.validator.web.dto.Xref;
import org.biopax.validator.utils.OntologyUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import static org.hamcrest.Matchers.*;

import java.io.IOException;

import static org.junit.Assert.*;


public class SuggesterTest {
  private static Suggester suggester;

  private final String URIEC = "http://identifiers.org/ec-code/";
  private final String URIGENE = "http://identifiers.org/ncbi.gene/";
  private final String EC = "ec";
  private final String NSEC = "ec-code:";
  private final String PREFEREC = "enzyme nomenclature";
  private final String ECCODE = "1.1.1.1";
  private final String NSGENE = "ncbi.gene:";
  private final String FOO = "foo";
  private final String GENEID = "1111";

  @org.junit.Rule
  public final ExpectedException exception = ExpectedException.none();
  //in a test, put exception.expect(UnsupportedOperationException.class)
  //before the line that should throw that exception.

  @BeforeClass
  public static void init() throws IOException {
    OntologyUtils utils = new OntologyUtils();
    //using ontologies from biopax-validator/src/test/resources
    utils.setOntologyConfig(PropertiesLoaderUtils.loadProperties(new ClassPathResource("test-obo.properties")));
    utils.init();
    suggester = new SuggesterService(utils, utils);
  }

  @Test
  public void getPrimaryDbName() {
    assertNull(suggester.getPrimaryDbName("ec_code"));
    assertEquals(PREFEREC, suggester.getPrimaryDbName(EC));
  }

  @Test
  public void xref() {
    Clue c = suggester.xref(null);
    assertNotNull(c);
    assertTrue(c.getValues().isEmpty());
    assertThat(c.getInfo(), equalTo("xref"));

    //x is a valid xref
    Xref x = new Xref();
    x.setUri(NSEC+ECCODE);
    x.setDb(EC);
    x.setId(ECCODE);
    //y has invalid xref.id
    Xref y = new Xref();
    y.setUri(NSEC+FOO);
    y.setDb(EC);
    y.setId(FOO);

    c = suggester.xref(new Xref[]{x, y});
    assertNotNull(c);
    assertThat(c.getValues().size(), equalTo(2));
    assertThat(c.getInfo(), equalTo("xref"));

    //TODO more assertions
  }

  @Test
  public void xrefDbIdToUri() {
    assertThat(suggester.xrefDbIdToUri(EC, ECCODE), equalTo(URIEC + ECCODE));

    exception.expect(IllegalArgumentException.class);
    suggester.xrefDbIdToUri(FOO, ECCODE);

    exception.expect(IllegalArgumentException.class);
    suggester.xrefDbIdToUri(EC, FOO);

    assertThat(suggester.xrefDbIdToUri("NCBI Gene", GENEID), equalTo(URIGENE + GENEID));
    // "entrez_gene" (non-standard name) should be auto-fixed, mapped to "ncbi gene"
    assertThat(suggester.xrefDbIdToUri("entrez_gene", GENEID), equalTo(URIGENE + GENEID));
  }
}