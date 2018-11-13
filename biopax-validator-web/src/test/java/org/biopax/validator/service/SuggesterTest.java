package org.biopax.validator.service;

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
  private static String URI_NS_EC = "http://identifiers.org/ec-code/";
  private static String URI_NS_GENE = "http://identifiers.org/ncbi.gene/";

  @org.junit.Rule
  public final ExpectedException exception = ExpectedException.none();

  @BeforeClass
  public static void init() throws IOException {
    OntologyUtils utils = new OntologyUtils(null,
      PropertiesLoaderUtils.loadProperties(new ClassPathResource("test-obo.properties")));//biopax-validator/src/test/resources
    utils.init();
    suggester = new SuggesterService(utils, utils);
  }

  @Test
  public void getPrimaryDbName() {
    assertNull(suggester.getPrimaryDbName("ec_code"));
    assertEquals("enzyme nomenclature", suggester.getPrimaryDbName("ec"));
  }

  @Test
  public void xref() {
    //TODO update assertion once method is implemented
    exception.expect(UnsupportedOperationException.class);
    suggester.xref();
  }

  @Test
  public void xrefDbIdToUri() {
    String ecCode = "1.1.1.1";
    String geneid = "1111";

    assertThat(suggester.xrefDbIdToUri("ec", ecCode), equalTo(URI_NS_EC + ecCode));

    exception.expect(IllegalArgumentException.class);
    suggester.xrefDbIdToUri("foo", ecCode);

    exception.expect(IllegalArgumentException.class);
    suggester.xrefDbIdToUri("ec", "foo");

    assertThat(suggester.xrefDbIdToUri("NCBI Gene", geneid), equalTo(URI_NS_GENE + geneid));
    // "entrez_gene" (non-standard name) should be auto-fixed, mapped to "ncbi gene"
    assertThat(suggester.xrefDbIdToUri("entrez_gene", geneid), equalTo(URI_NS_GENE + geneid));
  }
}