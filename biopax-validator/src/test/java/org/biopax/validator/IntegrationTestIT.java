package org.biopax.validator;

import static org.junit.Assert.*;

import java.io.*;
import java.util.*;

import org.biopax.validator.api.Validator;
import org.junit.*;
import org.junit.runner.RunWith;

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
import org.biopax.validator.rules.XrefSynonymDbRule;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Tests the BioPAX Validator.
 *
 * AspectJ LTW is disabled intentionally
 * (here we don't check for the rdf/xml parser errors)
 *
 * @author rodche
 */
@RunWith(SpringRunner.class)
@ContextConfiguration("classpath:META-INF/spring/appContext-validator.xml")
public class IntegrationTestIT {
  @Autowired
  private XrefUtils xrefUtils;

  @Autowired
  private Validator biopaxValidator;

  @Autowired
  private ApplicationContext context;

  private static BioPAXFactory factory3;
  private static SimpleIOHandler simpleIO; // to write OWL examples of what rule checks
  private static final String OUTPUT_DIR = IntegrationTestIT.class.getResource("").getPath();

  @BeforeClass
  public static void setUp() {
    factory3 = BioPAXLevel.L3.getDefaultFactory();
    simpleIO = new SimpleIOHandler(BioPAXLevel.L3);
  }

  @Test
  public void testBuildPaxtoolsL2ModelSimple() {
    System.out.println("with Level2 data");
    InputStream is = getClass().getResourceAsStream("biopax_id_557861_mTor_signaling.owl");
    SimpleIOHandler io = new SimpleIOHandler();
    io.mergeDuplicates(true);
    Model model = io.convertFromOWL(is);
    assertNotNull(model);
    assertFalse(model.getObjects().isEmpty());
  }

  @Test
  public void testBuildPaxtoolsL3ModelSimple() {
    System.out.println("with Level3 data");
    InputStream is = getClass().getResourceAsStream("biopax3-short-metabolic-pathway.owl");
    Validation validation = new Validation(new BiopaxIdentifier());
    biopaxValidator.importModel(validation, is);
    assertTrue(validation.getModel() instanceof Model);
    Model model = (Model) validation.getModel();
    assertFalse(model.getObjects().isEmpty());
    assertEquals(50,model.getObjects().size());
    biopaxValidator.validate(validation);
    assertFalse(validation.getError().isEmpty());
    assertEquals(3, validation.getError().size());
    assertEquals(16, validation.getTotalProblemsFound());
  }

  @Test
  public void testIsEquivalentUnificationXref() {
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

  /*
   * Tests:
   *
   * For 'GO', in the manuallyAddedDbSynonyms bean configuration
   * (see spring-context.xml) we have
   * 	<list>
   *		<value>GO</value>
   *		<value>GENE ONTOLOGY</value>
   *		<value>GENE_ONTOLOGY</value>
   *	</list>
   *
   * In the MIRIAM (resources/Miriam.xml) we have:
   *  <datatype id="MIR:00000022" startChar="^GO:\d{7}$">
   * 	<name>Gene OntologyAccess</name>
   *	<synonyms>
   * 		<synonym>GO</synonym>
   * 	</synonyms>
   *
   *  So, we want to test here that
   *  1. all three synonyms are present there;
   *  2. the primary one should be 'GENE ONTOLOGY';
   *  3. all three are assigned with the regexp: "^GO:\d{7}$".
   *
   */
  @Test
  public void testSynonymsWereRead() {
    List<String> gs = xrefUtils.getSynonymsForDbName("go");
    assertTrue(gs.contains("GO"));
    assertTrue(gs.contains("GENE ONTOLOGY"));
    assertTrue(gs.contains("GENE_ONTOLOGY"));
    assertTrue(xrefUtils.isUnofficialOrMisspelledDbName("GENE_ONTOLOGY"));
    assertFalse(xrefUtils.isUnofficialOrMisspelledDbName("GO"));
    assertTrue(xrefUtils.isUnofficialOrMisspelledDbName("medline"));
  }

  @Test
  public void testXRefHelperContainsSynonyms() {
    assertNotNull(xrefUtils.getPrimaryDbName("GO"));
    assertNotNull(xrefUtils.getPrimaryDbName("GENE ONTOLOGY"));
    assertNotNull(xrefUtils.getPrimaryDbName("GENE_ONTOLOGY"));
  }

  @Test
  public void testPrimarySynonym() {
    //not in Miriam: PIR
    assertEquals("UNIPROT KNOWLEDGEBASE", xrefUtils.getPrimaryDbName("pir"));
    //Miriam: Gene OntologyAccess
    assertEquals("GENE ONTOLOGY", xrefUtils.getPrimaryDbName("go"));
    assertEquals("KEGG COMPOUND", xrefUtils.getSynonymsForDbName("kegg compound").get(0));
    assertEquals("KEGG COMPOUND", xrefUtils.getPrimaryDbName("ligand"));
    assertEquals("KEGG GENOME", xrefUtils.getPrimaryDbName("kegg organism"));
    assertEquals("KYOTO ENCYCLOPEDIA OF GENES AND GENOMES", xrefUtils.getPrimaryDbName("KEGG"));
  }

  @Test
  public void testHasRegexp() {
    List<String> goes = xrefUtils.getSynonymsForDbName("go");
    for (String db : goes) {
      assertEquals("^GO:\\d{7}$", xrefUtils.getRegexpString(db));
    }
  }

  @Test
  public void testXrefIdTemplateMatch() {
    Xref xref = factory3.create(UnificationXref.class, "1");
    XrefRule r = (XrefRule) context.getBean("xrefRule");

    //XrefRule is trying to match 'GO:0005737'
    xref.setDb("GO");
    xref.setId("GO:0005737");
    r.check(null, xref);

    // XrefRule is trying to match 'XP_001075834'
    xref.setDb("RefSeq");
    xref.setId("XP_001075834");
    r.check(null, xref);
  }

  /*
   * There was a special case when the valid term "Phosphorylation"
   * (first symbol in upper case) was reported as error...
   * The test data was extracted from the Kumaran's "Catalysis" data.
   */
  @Test
  public void testSpecificCvFromFile() {
    InteractionTypeCvRule rule =
      (InteractionTypeCvRule) context.getBean("interactionTypeCvRule");

    assertFalse(rule.getValidTerms().isEmpty());

    InteractionVocabulary v = factory3.create(InteractionVocabulary.class, "okCVTerm");
    v.addTerm("Phosphorylation");
    v.addComment("Ok term: upper or lower case letters do not matter.");
    UnificationXref ux = factory3.create(UnificationXref.class, "UnificationXref_MI_0217");
    ux.setDb("MI");
    ux.setId("MI:0217");
    v.addXref(ux);
    rule.check(null, v);

    // what a surprise, the following used to fail (before it's been fixed)
    simpleIO.mergeDuplicates(true);
    Model m = simpleIO.convertFromOWL(getClass().getResourceAsStream("InteractionVocabulary-Phosphorylation.xml"));
    InteractionVocabulary vv = (InteractionVocabulary) m.getByID("Interaction_Phosphorylation");
    rule.check(null, vv);
  }


  @Test
  public void testCellularLocationRuleWrong() {
    CellularLocationCvRule instance =
      (CellularLocationCvRule) context.getBean("cellularLocationCvRule");
    CellularLocationVocabulary lcv = factory3.create(CellularLocationVocabulary.class, "badTerm");
    lcv.addTerm("LOCATION?");
    assertTrue(instance.canCheck(lcv));
    Validation v = new Validation(new BiopaxIdentifier());
    instance.check(v, lcv);
    assertEquals(1, v.countErrors(lcv.getUri(), null, null, null,
      false, false));
  }

  @Test
  public void testCellularLocationRule() {
    CellularLocationCvRule instance =
      (CellularLocationCvRule) context.getBean("cellularLocationCvRule");
    CellularLocationVocabulary cl = factory3.create(CellularLocationVocabulary.class, "cytoplasm");
    cl.addTerm("cytoplasm");
    UnificationXref ux = factory3.create(UnificationXref.class, "UnificationXref_GO_0005737");
    ux.setDb("GO");
    ux.setId("GO:0005737");
    cl.addXref(ux);

    Validation v = new Validation(new BiopaxIdentifier());
    instance.check(v, cl);
    assertTrue(v.getError().isEmpty());
  }

  @Test
  public void testXrefRuleWrong() {
    XrefRule instance = (XrefRule) context.getBean("xrefRule");
    UnificationXref x = factory3.create(UnificationXref.class, "1");
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
  public void testXrefRule() {
    XrefRule instance = (XrefRule) context.getBean("xrefRule");
    UnificationXref x = factory3.create(UnificationXref.class, "1");
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
  public void testXrefRuleEntezGene() {
    XrefRule instance = (XrefRule) context.getBean("xrefRule");
    UnificationXref x = factory3.create(UnificationXref.class, "1");
    x.setDb("EntrezGene");
    x.setId("0000000");
    Validation v = new Validation(new BiopaxIdentifier());
    instance.check(v, x);
    assertTrue(v.getError().isEmpty());
  }

  @Test
  public void testInteractionTypeRule() throws FileNotFoundException {
    InteractionTypeCvRule instance =
      (InteractionTypeCvRule) context.getBean("interactionTypeCvRule");

    Model m = factory3.createModel();

    InteractionVocabulary iv = factory3.create(InteractionVocabulary.class, "preferredCVTerm");
    iv.addTerm("phosphorylation reaction");
    iv.addComment("Preferred term");

    UnificationXref ux = factory3.create(UnificationXref.class, "UnificationXref_MI_0217");
    ux.setDb("MI");
    ux.setId("MI:0217");
    iv.addXref(ux);

    m.add(ux);
    m.add(iv);

    Validation v = new Validation(new BiopaxIdentifier());
    instance.check(v, iv);
    assertTrue(v.getError().isEmpty());

    iv = factory3.create(InteractionVocabulary.class, "synonymCVTerm");
    iv.addTerm("phosphorylation");
    iv.addComment("Valid term");
    m.add(iv);
    iv.addXref(ux);

    v = new Validation(new BiopaxIdentifier());
    instance.check(v, iv);
    assertTrue(v.getError().isEmpty());

    iv = factory3.create(InteractionVocabulary.class, "okCVTerm");
    iv.addTerm("Phosphorylation");
    iv.addComment("Ok term: upper or lower case letters do not matter.");
    m.add(iv);
    iv.addXref(ux);

    v = new Validation(new BiopaxIdentifier());
    instance.check(v, iv);
    assertTrue(v.getError().isEmpty());

    iv = factory3.create(InteractionVocabulary.class, "invalidCVTerm");
    iv.addTerm("phosphorylated residue");
    iv.addComment("Invalid term from MOD (very similar to MI one, however)");
    ux = factory3.create(UnificationXref.class, "UnificationXref_MOD_00696");
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

    simpleIO.convertToOWL(m, new FileOutputStream(OUTPUT_DIR + File.separator + "testInteractionTypeRule.owl"));
  }

  @Test
  public void testProteinModificationFeatureCvRule() {
    ProteinModificationFeatureCvRule rule =
      (ProteinModificationFeatureCvRule) context.getBean("proteinModificationFeatureCvRule");
    //System.out.print("proteinModificationFeatureCvRule valid terms are: "
    //		+ rule.getValidTerms().toString());
    assertTrue(rule.getValidTerms().contains("(2S,3R)-3-hydroxyaspartic acid".toLowerCase()));

    SequenceModificationVocabulary cv = factory3.create(SequenceModificationVocabulary.class, "MOD_00036");
    cv.addTerm("(2S,3R)-3-hydroxyaspartic acid");
    ModificationFeature mf = factory3.create(ModificationFeature.class, "MF_MOD_00036");
    mf.setModificationType(cv);

    UnificationXref ux = factory3.create(UnificationXref.class, "UnificationXref_MOD_00036");
    ux.setDb("MOD");
    ux.setId("MOD:00036");
    cv.addXref(ux);

    Validation v = new Validation(new BiopaxIdentifier());
    rule.check(v, mf); // should not fail
    assertTrue(v.getError().isEmpty());
  }


  @Test
  public void testXrefSynonymDbRule() {
    XrefSynonymDbRule instance = (XrefSynonymDbRule) context.getBean("xrefSynonymDbRule");
//        instance.setBehavior(Behavior.ERROR);
    UnificationXref x = factory3.create(UnificationXref.class, "1");
    //use an unofficial/misspelled name
    x.setDb("entrez_gene");
    x.setId("0000000");
    Validation v = new Validation(new BiopaxIdentifier());
    instance.check(v, x);
    assertEquals(1, v.countErrors(x.getUri(), null, "db.name.spelling",
      null, false, false));

    // use one of its official synonyms
    x.setDb("entre-zgene");
    x.setId("0000000");
    v = new Validation(new BiopaxIdentifier());
    instance.check(v, x);
    assertTrue(v.getError().isEmpty());

    //use an unofficial/misspelled name
    x.setDb("gene_ontology");
    x.setId("0000000");
    v = new Validation(new BiopaxIdentifier());
    instance.check(v, x);
    assertEquals(1, v.countErrors(x.getUri(), null, null,
      null, false, false));
    // use one of its official synonyms
    x.setDb("go");
    x.setId("0000000");
    v = new Validation(new BiopaxIdentifier());
    instance.check(v, x);
    assertTrue(v.getError().isEmpty());
  }

}
