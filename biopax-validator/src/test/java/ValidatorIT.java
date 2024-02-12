import java.io.*;
import java.util.*;

import org.biopax.validator.BiopaxIdentifier;
import org.biopax.validator.XrefUtils;
import org.biopax.validator.api.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.BioPAXFactory;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.*;
import org.biopax.validator.api.beans.Validation;
import org.biopax.validator.rules.CellularLocationCvRule;
import org.biopax.validator.rules.InteractionTypeCvRule;
import org.biopax.validator.rules.ProteinModificationFeatureCvRule;
import org.biopax.validator.rules.XrefRule;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the BioPAX Validator.
 *
 * @author rodche
 */
public class ValidatorIT {

  static XrefUtils xrefUtils;
  static Validator biopaxValidator;
  static ApplicationContext context;


  @BeforeAll
  static void init() {
    context = new ClassPathXmlApplicationContext(
//        "META-INF/spring/appContext-loadTimeWeaving.xml", //AspectJ LTW is disabled (won't check for the rdf/xml parser errors)
        "META-INF/spring/appContext-validator.xml");
    // Rules are now loaded, and AOP is listening for BioPAX model method calls.
    biopaxValidator = (Validator) context.getBean("biopaxValidator");
    xrefUtils = (XrefUtils) context.getBean("ontologyUtils");
  }

  private static final String OUTPUT_DIR = ValidatorIT.class.getResource("").getPath();

  @Test
  public void buildPaxtoolsL2ModelSimple() {
    InputStream is = getClass().getResourceAsStream("/biopax_id_557861_mTor_signaling.owl");
    SimpleIOHandler io = new SimpleIOHandler();
    io.mergeDuplicates(true);
    Model model = io.convertFromOWL(is);
    assertNotNull(model);
    assertFalse(model.getObjects().isEmpty());
  }

  @Test
  public void buildPaxtoolsL3ModelSimple() {
    InputStream is = getClass().getResourceAsStream("/biopax3-short-metabolic-pathway.owl");
    Validation validation = new Validation(new BiopaxIdentifier());
    biopaxValidator.importModel(validation, is);
    assertTrue(validation.getModel() instanceof Model);
    Model model = (Model) validation.getModel();
    assertFalse(model.getObjects().isEmpty());
    assertEquals(50, model.getObjects().size());
    biopaxValidator.validate(validation);
    assertFalse(validation.getError().isEmpty());
    assertEquals(3, validation.getError().size());
    assertEquals(16, validation.getTotalProblemsFound());
  }

  @Test
  public void isEquivalentUnificationXref() {
    BioPAXFactory factory3 = BioPAXLevel.L3.getDefaultFactory();
    UnificationXref x1 = factory3.create(UnificationXref.class, "x1");
    x1.addComment("x1");
    x1.setDb("db");
    x1.setId("id");
    UnificationXref x2 = factory3.create(UnificationXref.class, "x2");
    x2.addComment("x2");
    x2.setDb("db");
    x2.setId("id");

    assertTrue(x1.isEquivalent(x2));

    UnificationXref x3 = factory3.create(UnificationXref.class, "x1");
    x3.addComment("x3");
    x3.setDb(null);
    x3.setId("foo");

    assertTrue(x1.isEquivalent(x3)); //only ID and type matter as 'equals','hashCode' were overridden in Paxtools
  }


  @Test
  public void synonymsWereRead() {
    List<String> gs = xrefUtils.getSynonymsForDbName("go");
    assertAll(
        () -> assertTrue(gs.contains("GO")),
        () -> assertTrue(gs.contains("GENE ONTOLOGY")),
        () -> assertTrue(xrefUtils.isUnofficialOrMisspelledDbName("GENE_ONTOLOGY")),
        () -> assertFalse(xrefUtils.isUnofficialOrMisspelledDbName("GO")),
        () -> assertFalse(xrefUtils.isUnofficialOrMisspelledDbName("medline"))
    );
  }

  @Test
  public void xRefHelperContainsSynonyms() {
    assertAll(
        () -> assertNotNull(xrefUtils.getPrimaryDbName("GO")),
        () -> assertNotNull(xrefUtils.getPrimaryDbName("GENE ONTOLOGY"))
    );
  }

  @Test
  public void primarySynonym() {
    assertAll(
        () -> assertEquals("UNIPROT PROTEIN", xrefUtils.getPrimaryDbName("pir")), //not in registry: PIR (using extra synonyms)
        () -> assertEquals("GENE ONTOLOGY", xrefUtils.getPrimaryDbName("go")),
        () -> assertEquals("KEGG.COMPOUND", xrefUtils.getSynonymsForDbName("kegg compound").get(0)),
        () -> assertEquals("KEGG COMPOUND", xrefUtils.getSynonymsForDbName("kegg compound").get(1)),
        () -> assertEquals("KEGG COMPOUND", xrefUtils.getPrimaryDbName("ligand")), //ligand (deprecated) is inside kegg compound!
        () -> assertEquals("KEGG GENOME", xrefUtils.getPrimaryDbName("kegg organism")),
        () -> assertEquals("KYOTO ENCYCLOPEDIA OF GENES AND GENOMES", xrefUtils.getPrimaryDbName("KEGG"))
    );
  }

  @Test
  public void prefix() {
    assertAll(
        () -> assertEquals("uniprot", xrefUtils.getPrefix("pir")),
        () -> assertEquals("go", xrefUtils.getPrefix("go")),
        () -> assertEquals("kegg.compound", xrefUtils.getPrefix("ligand")),
        () -> assertEquals("kegg.genome", xrefUtils.getPrefix("kegg organism")),
        () -> assertEquals("kegg", xrefUtils.getPrefix("KEGG"))
    );
  }

  @ParameterizedTest
  @MethodSource
  public void hasRegexp(String db) {
      assertEquals("^\\d{7}$", xrefUtils.getRegexpString(db));
  }
  static List<String> hasRegexp() { //source of args for the above parameterized test
    return xrefUtils.getSynonymsForDbName("go");
  }

  @Test
  public void xrefIdTemplateMatchOK() {
    Xref xref = BioPAXLevel.L3.getDefaultFactory().create(UnificationXref.class, "1");
    XrefRule r = (XrefRule) context.getBean("xrefRule");

    Validation v = new Validation(new BiopaxIdentifier());
    //XrefRule is trying to match 'GO:0005737'
    xref.setDb("GO");
    xref.setId("GO:0005737");
    r.check(v, xref);

    // XrefRule is trying to match 'XP_001075834'
    xref.setDb("RefSeq");
    xref.setId("XP_001075834");
    r.check(v, xref);

    assertTrue(v.getError().isEmpty());
  }

  /*
   * There was a special case when the valid term "Phosphorylation"
   * (first symbol in upper case) was reported as error...
   * The test data was extracted from the Kumaran's "Catalysis" data.
   */
  @Test
  public void specificCvFromFile() {
    InteractionTypeCvRule rule =
      (InteractionTypeCvRule) context.getBean("interactionTypeCvRule");

    assertFalse(rule.getValidTerms().isEmpty());

    Validation validation = new Validation(new BiopaxIdentifier());

    InteractionVocabulary v = BioPAXLevel.L3.getDefaultFactory().create(InteractionVocabulary.class, "okCVTerm");
    v.addTerm("Phosphorylation");
    v.addComment("Ok term: upper or lower case letters do not matter.");
    UnificationXref ux = BioPAXLevel.L3.getDefaultFactory().create(UnificationXref.class, "UnificationXref_MI_0217");
    ux.setDb("MI");
    ux.setId("MI:0217");
    v.addXref(ux);
    rule.check(validation, v);

    // what a surprise, the following used to fail (before it's been fixed)
    SimpleIOHandler simpleIO = new SimpleIOHandler(BioPAXLevel.L3);
    simpleIO.mergeDuplicates(true);
    Model m = simpleIO.convertFromOWL(getClass().getResourceAsStream("/InteractionVocabulary-Phosphorylation.xml"));
    InteractionVocabulary vv = (InteractionVocabulary) m.getByID("Interaction_Phosphorylation");
    rule.check(validation, vv);

    assertTrue(validation.getError().isEmpty());
  }


  @Test
  public void cellularLocationRuleWrong() {
    CellularLocationCvRule instance =
      (CellularLocationCvRule) context.getBean("cellularLocationCvRule");
    CellularLocationVocabulary lcv = BioPAXLevel.L3.getDefaultFactory().create(CellularLocationVocabulary.class, "badTerm");
    lcv.addTerm("LOCATION?");
    assertTrue(instance.canCheck(lcv));
    Validation v = new Validation(new BiopaxIdentifier());
    instance.check(v, lcv);
    assertEquals(1, v.countErrors(lcv.getUri(), null, null, null,
      false, false));
  }

  @Test
  public void cellularLocationRule() {
    CellularLocationCvRule instance =
      (CellularLocationCvRule) context.getBean("cellularLocationCvRule");
    CellularLocationVocabulary cl = BioPAXLevel.L3.getDefaultFactory().create(CellularLocationVocabulary.class, "cytoplasm");
    cl.addTerm("cytoplasm");
    UnificationXref ux = BioPAXLevel.L3.getDefaultFactory().create(UnificationXref.class, "UnificationXref_GO_0005737");
    ux.setDb("GO");
    ux.setId("GO:0005737");
    cl.addXref(ux);

    Validation v = new Validation(new BiopaxIdentifier());
    instance.check(v, cl);
    assertTrue(v.getError().isEmpty());
  }

  @Test
  public void xrefRuleWrong() {
    XrefRule instance = (XrefRule) context.getBean("xrefRule");
    UnificationXref x = BioPAXLevel.L3.getDefaultFactory().create(UnificationXref.class, "1");
    x.setDb("ILLEGAL DB NAME");
    Validation v = new Validation(new BiopaxIdentifier());
    instance.check(v, x);
    assertEquals(1, v.countErrors(x.getUri(), null, "unknown.db", null,
      false, false));

    x.setDb("NCBI"); //ambiguous
    v = new Validation(new BiopaxIdentifier());
    instance.check(v, x);
    assertEquals(1, v.countErrors(x.getUri(), null, "unknown.db", null,
      false, false));
  }


  @Test
  public void xrefRule() {
    XrefRule instance = (XrefRule) context.getBean("xrefRule");
    UnificationXref x = BioPAXLevel.L3.getDefaultFactory().create(UnificationXref.class, "1");
    x.setDb("reactome");
    x.setId("0000000");
    Validation v = new Validation(new BiopaxIdentifier());
    instance.check(v, x);
    assertEquals(1, v.countErrors(x.getUri(), null, "invalid.id.format", null,
      false, false));
  }

  /*
   * Special case - check synonyms are there
   */
  @Test
  public void xrefRuleEntezGene() {
    XrefRule instance = (XrefRule) context.getBean("xrefRule");
    UnificationXref x = BioPAXLevel.L3.getDefaultFactory().create(UnificationXref.class, "1");
    x.setDb("EntrezGene");
    x.setId("0000000");
    Validation v = new Validation(new BiopaxIdentifier());
    instance.check(v, x);
    assertTrue(v.getError().isEmpty());
  }

  @Test
  public void interactionTypeRule() throws FileNotFoundException {
    InteractionTypeCvRule instance =
      (InteractionTypeCvRule) context.getBean("interactionTypeCvRule");

    Model m = BioPAXLevel.L3.getDefaultFactory().createModel();

    InteractionVocabulary iv = BioPAXLevel.L3.getDefaultFactory().create(InteractionVocabulary.class, "preferredCVTerm");
    iv.addTerm("phosphorylation reaction");
    iv.addComment("Preferred term");

    UnificationXref ux = BioPAXLevel.L3.getDefaultFactory().create(UnificationXref.class, "UnificationXref_MI_0217");
    ux.setDb("MI");
    ux.setId("MI:0217");
    iv.addXref(ux);

    m.add(ux);
    m.add(iv);

    Validation v = new Validation(new BiopaxIdentifier());
    instance.check(v, iv);
    assertTrue(v.getError().isEmpty());

    iv = BioPAXLevel.L3.getDefaultFactory().create(InteractionVocabulary.class, "synonymCVTerm");
    iv.addTerm("phosphorylation");
    iv.addComment("Valid term");
    m.add(iv);
    iv.addXref(ux);

    v = new Validation(new BiopaxIdentifier());
    instance.check(v, iv);
    assertTrue(v.getError().isEmpty());

    iv = BioPAXLevel.L3.getDefaultFactory().create(InteractionVocabulary.class, "okCVTerm");
    iv.addTerm("Phosphorylation");
    iv.addComment("Ok term: upper or lower case letters do not matter.");
    m.add(iv);
    iv.addXref(ux);

    v = new Validation(new BiopaxIdentifier());
    instance.check(v, iv);
    assertTrue(v.getError().isEmpty());

    iv = BioPAXLevel.L3.getDefaultFactory().create(InteractionVocabulary.class, "invalidCVTerm");
    iv.addTerm("phosphorylated residue");
    iv.addComment("Invalid term from MOD (very similar to MI one, however)");
    ux = BioPAXLevel.L3.getDefaultFactory().create(UnificationXref.class, "UnificationXref_MOD_00696");
    ux.setDb("MOD");
    ux.setId("MOD:00696");
    /* Note: in fact, both MOD:00696 and MI:0217 have synonym name "Phosphorylation"!
    	TODO Validator (currently) checks names in the CV 'term' property only,
    	but it also should check what can be inferred from the xref.id!
    */
    iv.addXref(ux);
    m.add(ux);
    m.add(iv);

    v = new Validation(new BiopaxIdentifier());
    instance.check(v, iv);
    assertEquals(1, v.countErrors(iv.getUri(), null, "illegal.cv.term",
      null, false, false));
    SimpleIOHandler simpleIO = new SimpleIOHandler(BioPAXLevel.L3);
    simpleIO.convertToOWL(m, new FileOutputStream(OUTPUT_DIR + File.separator + "testInteractionTypeRule.owl"));
  }

  @Test
  public void proteinModificationFeatureCvRule() {
    ProteinModificationFeatureCvRule rule =
      (ProteinModificationFeatureCvRule) context.getBean("proteinModificationFeatureCvRule");

    assertTrue(rule.getValidTerms().contains("(2S,3R)-3-hydroxyaspartic acid".toLowerCase()));

    SequenceModificationVocabulary cv = BioPAXLevel.L3.getDefaultFactory().create(SequenceModificationVocabulary.class, "MOD_00036");
    cv.addTerm("(2S,3R)-3-hydroxyaspartic acid");
    ModificationFeature mf = BioPAXLevel.L3.getDefaultFactory().create(ModificationFeature.class, "MF_MOD_00036");
    mf.setModificationType(cv);

    UnificationXref ux = BioPAXLevel.L3.getDefaultFactory().create(UnificationXref.class, "UnificationXref_MOD_00036");
    ux.setDb("MOD");
    ux.setId("MOD:00036");
    cv.addXref(ux);

    Validation v = new Validation(new BiopaxIdentifier());
    rule.check(v, mf); // should not fail
    assertTrue(v.getError().isEmpty());
  }
}
