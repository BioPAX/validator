package org.biopax.validator;

import java.io.*;
import java.util.*;

import org.biopax.validator.api.Validator;
import org.junit.jupiter.api.Assertions;
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

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


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
    Assertions.assertNotNull(model);
    Assertions.assertFalse(model.getObjects().isEmpty());
  }

  @Test
  public void buildPaxtoolsL3ModelSimple() {
    InputStream is = getClass().getResourceAsStream("/biopax3-short-metabolic-pathway.owl");
    Validation validation = new Validation(new BiopaxIdentifier());
    biopaxValidator.importModel(validation, is);
    Assertions.assertTrue(validation.getModel() instanceof Model);
    Model model = (Model) validation.getModel();
    Assertions.assertFalse(model.getObjects().isEmpty());
    Assertions.assertEquals(50, model.getObjects().size());
    biopaxValidator.validate(validation);
    Assertions.assertFalse(validation.getError().isEmpty());
    Assertions.assertEquals(3, validation.getError().size());
    Assertions.assertEquals(16, validation.getTotalProblemsFound());
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

    Assertions.assertTrue(x1.isEquivalent(x2));

    UnificationXref x3 = factory3.create(UnificationXref.class, "x1");
    x3.addComment("x3");
    x3.setDb(null);
    x3.setId("foo");

    Assertions.assertTrue(x1.isEquivalent(x3)); //only ID and type matter as 'equals','hashCode' were overridden in Paxtools
  }


  @Test
  public void synonymsWereRead() {
    List<String> gs = xrefUtils.getSynonymsForDbName("go");
    Assertions.assertTrue(gs.contains("GO"));
    Assertions.assertTrue(gs.contains("GENE ONTOLOGY"));
    Assertions.assertTrue(xrefUtils.isUnofficialOrMisspelledDbName("GENE_ONTOLOGY"));
    Assertions.assertFalse(xrefUtils.isUnofficialOrMisspelledDbName("GO"));
    Assertions.assertFalse(xrefUtils.isUnofficialOrMisspelledDbName("medline"));
  }

  @Test
  public void xRefHelperContainsSynonyms() {
    Assertions.assertNotNull(xrefUtils.getPrimaryDbName("GO"));
    Assertions.assertNotNull(xrefUtils.getPrimaryDbName("GENE ONTOLOGY"));
  }

  @Test
  public void primarySynonym() {
    //not in registry: PIR
    Assertions.assertEquals("UNIPROT PROTEIN", xrefUtils.getPrimaryDbName("pir"));
    Assertions.assertEquals("GENE ONTOLOGY", xrefUtils.getPrimaryDbName("go"));
    Assertions.assertEquals("KEGG COMPOUND", xrefUtils.getSynonymsForDbName("kegg compound").get(0));
    Assertions.assertEquals("KEGG COMPOUND", xrefUtils.getPrimaryDbName("ligand")); //ligand (deprecated) is inside kegg compound!
    Assertions.assertEquals("KEGG GENOME", xrefUtils.getPrimaryDbName("kegg organism"));
    Assertions.assertEquals("KYOTO ENCYCLOPEDIA OF GENES AND GENOMES", xrefUtils.getPrimaryDbName("KEGG"));
  }

  @Test
  public void hasRegexp() {
    List<String> goes = xrefUtils.getSynonymsForDbName("go");
    for (String db : goes) {
      Assertions.assertEquals("^\\d{7}$", xrefUtils.getRegexpString(db));
    }
  }

  @Test
  public void xrefIdTemplateMatch() {
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

    Assertions.assertFalse(rule.getValidTerms().isEmpty());

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
  }


  @Test
  public void cellularLocationRuleWrong() {
    CellularLocationCvRule instance =
      (CellularLocationCvRule) context.getBean("cellularLocationCvRule");
    CellularLocationVocabulary lcv = BioPAXLevel.L3.getDefaultFactory().create(CellularLocationVocabulary.class, "badTerm");
    lcv.addTerm("LOCATION?");
    Assertions.assertTrue(instance.canCheck(lcv));
    Validation v = new Validation(new BiopaxIdentifier());
    instance.check(v, lcv);
    Assertions.assertEquals(1, v.countErrors(lcv.getUri(), null, null, null,
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
    Assertions.assertTrue(v.getError().isEmpty());
  }

  @Test
  public void xrefRuleWrong() {
    XrefRule instance = (XrefRule) context.getBean("xrefRule");
    UnificationXref x = BioPAXLevel.L3.getDefaultFactory().create(UnificationXref.class, "1");
    x.setDb("ILLEGAL DB NAME");
    Validation v = new Validation(new BiopaxIdentifier());
    instance.check(v, x);
    Assertions.assertEquals(1, v.countErrors(x.getUri(), null, "unknown.db", null,
      false, false));

    x.setDb("NCBI"); //ambiguous
    v = new Validation(new BiopaxIdentifier());
    instance.check(v, x);
    Assertions.assertEquals(1, v.countErrors(x.getUri(), null, "unknown.db", null,
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
    Assertions.assertEquals(1, v.countErrors(x.getUri(), null, "invalid.id.format", null,
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
    Assertions.assertTrue(v.getError().isEmpty());
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
    Assertions.assertTrue(v.getError().isEmpty());

    iv = BioPAXLevel.L3.getDefaultFactory().create(InteractionVocabulary.class, "synonymCVTerm");
    iv.addTerm("phosphorylation");
    iv.addComment("Valid term");
    m.add(iv);
    iv.addXref(ux);

    v = new Validation(new BiopaxIdentifier());
    instance.check(v, iv);
    Assertions.assertTrue(v.getError().isEmpty());

    iv = BioPAXLevel.L3.getDefaultFactory().create(InteractionVocabulary.class, "okCVTerm");
    iv.addTerm("Phosphorylation");
    iv.addComment("Ok term: upper or lower case letters do not matter.");
    m.add(iv);
    iv.addXref(ux);

    v = new Validation(new BiopaxIdentifier());
    instance.check(v, iv);
    Assertions.assertTrue(v.getError().isEmpty());

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
    Assertions.assertEquals(1, v.countErrors(iv.getUri(), null, "illegal.cv.term",
      null, false, false));
    SimpleIOHandler simpleIO = new SimpleIOHandler(BioPAXLevel.L3);
    simpleIO.convertToOWL(m, new FileOutputStream(OUTPUT_DIR + File.separator + "testInteractionTypeRule.owl"));
  }

  @Test
  public void proteinModificationFeatureCvRule() {
    ProteinModificationFeatureCvRule rule =
      (ProteinModificationFeatureCvRule) context.getBean("proteinModificationFeatureCvRule");

    Assertions.assertTrue(rule.getValidTerms().contains("(2S,3R)-3-hydroxyaspartic acid".toLowerCase()));

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
    Assertions.assertTrue(v.getError().isEmpty());
  }
}
