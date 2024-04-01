package org.biopax.validator.web.service;

import org.biopax.validator.web.dto.Clue;
import org.biopax.validator.web.dto.Xref;
import org.biopax.validator.utils.OntologyUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class SuggesterTest {
  private static Suggester suggester;

  private final String URIEC = "http://bioregistry.io/eccode:";
  private final String URIGENE = "http://bioregistry.io/ncbigene:";
  private final String EC = "ec";
  private final String NSEC = "eccode";
  private final String PREFEREC = "enzyme commission code";
  private final String ECCODE = "1.1.1.1";
  private final String FOO = "foo";
  private final String GENEID = "1111";

  @BeforeAll
  public static void init() throws IOException {
    OntologyUtils utils = new OntologyUtils();
    //using ontologies from biopax-validator/src/test/resources
    utils.setOntologyConfig(PropertiesLoaderUtils.loadProperties(new ClassPathResource("test-obo.properties")));
    utils.init();
    suggester = new SuggesterService(utils, utils);
  }

  @Test
  public void getPrimaryDbName() {
    assertEquals(PREFEREC, suggester.getPrimaryDbName("ec_code")); //it's now detected as synonym
    assertEquals(PREFEREC, suggester.getPrimaryDbName(EC));
  }

  @Test
  public void xref() {
    //x is a valid xref
    Xref x = new Xref();
    x.setDb(EC);
    x.setId(ECCODE);
    //y has invalid xref.id
    Xref y = new Xref();
    y.setDb(EC);
    y.setId(FOO);

    final Clue c = suggester.xref(new Xref[]{x, y});
    assertAll(
      () -> assertThrows(IllegalArgumentException.class ,()->suggester.xref(null)),
      () -> assertNotNull(c),
      () -> assertEquals(2, c.getValues().size()),
      () -> assertTrue(c.getInfo().startsWith("Checked")),
      () -> assertTrue(((Xref)c.getValues().get(0)).isDbOk()),
      () -> assertTrue(((Xref)c.getValues().get(0)).isIdOk()),
      () -> assertEquals(NSEC, ((Xref)c.getValues().get(0)).getNamespace()),
      () -> assertEquals(URIEC + ECCODE, ((Xref)c.getValues().get(0)).getUri()),
      () -> assertEquals(PREFEREC, ((Xref)c.getValues().get(0)).getPreferredDb()),
      () -> assertTrue(((Xref)c.getValues().get(1)).isDbOk()), //ok
      () -> assertEquals(PREFEREC, ((Xref)c.getValues().get(1)).getPreferredDb()),
      () -> assertEquals(NSEC, ((Xref)c.getValues().get(1)).getNamespace()),
      () -> assertFalse(((Xref)c.getValues().get(1)).isIdOk()), //not ok
      () -> assertNull(((Xref)c.getValues().get(1)).getUri()) //null
    );
  }

  @Test
  public void xrefDbIdToUri() {
    assertAll(
      () -> assertEquals(URIEC + ECCODE, suggester.xrefDbIdToUri(EC, ECCODE)),
      () -> assertThrows(IllegalArgumentException.class ,()->suggester.xrefDbIdToUri(FOO, ECCODE)),
      () -> assertThrows(IllegalArgumentException.class ,()->suggester.xrefDbIdToUri(EC, FOO)),
      () -> assertEquals(URIGENE + GENEID, suggester.xrefDbIdToUri("NCBI Gene", GENEID)),
      // "entrez gene" (non-standard name) should be auto-fixed, mapped to "ncbigene"
      () -> assertEquals(URIGENE + GENEID, suggester.xrefDbIdToUri("entrez gene", GENEID))
    );
  }
}