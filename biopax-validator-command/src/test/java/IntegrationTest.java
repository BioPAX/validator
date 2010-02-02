import static org.junit.Assert.*;

import java.util.*;

import org.junit.*;
import org.junit.runner.RunWith;

import java.io.FileOutputStream;
import java.io.IOException;

import org.biopax.paxtools.impl.level3.Level3FactoryImpl;
import org.biopax.paxtools.io.simpleIO.SimpleReader;
import org.biopax.paxtools.io.simpleIO.SimpleExporter;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.*;
import org.biopax.validator.Behavior;
import org.biopax.validator.rules.InteractionTypeCvRule;
import org.biopax.validator.rules.XrefRule;
import org.biopax.validator.utils.BiopaxValidatorException;
import org.biopax.validator.utils.XrefHelper;
import org.biopax.validator.rules.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author rodche
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:validator-core-context.xml"}) // AOP disabled
public class IntegrationTest {
    static final String TEST_PATHWAY = "biopax3-short-metabolic-pathway.owl";
    
    @Autowired
    XrefHelper xrefHelper;
    
    @Autowired
    ApplicationContext context;
    
    Level3FactoryImpl factory3;
	SimpleExporter exporter; // to write OWL examples of what rule checks
	final static String OUTDIR = "target";
	
    
    @Before
    public void setUp() {
        factory3 = new Level3FactoryImpl();
        exporter = new SimpleExporter(BioPAXLevel.L3);
    }

    @Test
    public void testRange1() {
        Evidence ev = (Evidence) factory3.createEvidence();
        EvidenceCodeVocabulary ec = factory3.createEvidenceCodeVocabulary();
        ev.addEvidenceCode(ec);
        /**
         * TODO check correct types are actually in such Set,
         * because the following works but shouldn't!
         */
        ControlledVocabulary cv = (CellVocabulary) factory3.createCellVocabulary();
        Set<ControlledVocabulary> set = new HashSet<ControlledVocabulary>(); // not Set<EvidenceCodeVocabulary>
        set.add(ec);
        set.add(cv);
        //ev.setEvidenceCode(set); // compile-time error       
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
	 * 	<name>Gene Ontology</name>
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
    	System.out.println("testSynonymsWereRead");
    	List<String> gs = xrefHelper.getSynonymsForDbName("go"); 	
    	assertTrue(gs.contains("GO"));
    	assertTrue(gs.contains("GENE ONTOLOGY"));
    	assertTrue(gs.contains("GENE_ONTOLOGY"));
    }

    @Test
    public void testXRefHelperContainsSynonyms() {
    	System.out.println("testXRefHelperContainsSynonyms");	
    	assertTrue(xrefHelper.contains("GO"));
    	assertTrue(xrefHelper.contains("GENE ONTOLOGY"));
    	assertTrue(xrefHelper.contains("GENE_ONTOLOGY"));
    }
    
    @Test
    public void testPrimarySynonym() {
    	System.out.println("testPrimarySynonym");  	
    	System.out.println("not in Miriam: PIR");
    	assertEquals("UNIPROT", xrefHelper.getSynonymsForDbName("pir").get(0));
    	System.out.println("Miriam: Gene Ontology");
    	assertEquals("GENE ONTOLOGY", xrefHelper.getSynonymsForDbName("go").get(0));
    }
    
    @Test
    public void testHasRegexp() {
    	System.out.println("testHasRegexp");
    	List<String> goes = xrefHelper.getSynonymsForDbName("go");
    	for(String db: goes) {
    		assertEquals("^GO:\\d{7}$", xrefHelper.getRegexpString(db));
    	}
    }
    
    @Test
    public void testXrefIdTemplateMatch() {
 	   Xref xref = factory3.createUnificationXref();
 	   XrefRule r = (XrefRule) context.getBean("xrefRule");
 	   
 	   System.out.println("XrefRule is trying to match 'GO:0005737' against " 
 			   + xrefHelper.getRegexpString("GO"));
 	   xref.setDb("GO");
 	   xref.setId("GO:0005737");
	   r.check(xref);

 	   // one more
 	   System.out.println("XrefRule is trying to match 'XP_001075834' against " 
 			   + xrefHelper.getRegexpString("RefSeq"));
 	   xref.setDb("RefSeq");
 	   xref.setId("XP_001075834");
	   r.check(xref);
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
    	
    	System.out.println(rule.getValidTerms().toString());
    	
    	
        InteractionVocabulary v = factory3.createInteractionVocabulary();
        v.addTerm("Phosphorylation");
        v.addComment("Ok term: upper or lower case letters do not matter.");
        v.setRDFId("okCVTerm");
        rule.check(v);
    	
    	// but... what a surprise, the following may fail!

        SimpleReader r = new SimpleReader(BioPAXLevel.L3);
    	Model m = r.convertFromOWL(getClass().getResourceAsStream("/data/IntercationVocabulary-Phosphorylation.xml"));
    	InteractionVocabulary vv = (InteractionVocabulary) m.getByID("Interaction_Phosphorylation");
        rule.check(vv);
    }
    
    @Test(expected=BiopaxValidatorException.class)
    public void testCellularLocationRuleWrong() {
        CellularLocationCvRule instance =  
                (CellularLocationCvRule) context.getBean("cellularLocationCvRule");
        instance.setBehavior(Behavior.ERROR);  
        CellularLocationVocabulary lcv = factory3.createCellularLocationVocabulary();
        lcv.addTerm("LOCATION?");
        lcv.setRDFId("badTerm");    
        assertTrue(instance.canCheck(lcv));
       	instance.check(lcv);
    }
    
    @Test
    public void testCellularLocationRule() {
        CellularLocationCvRule instance =  
                (CellularLocationCvRule) context.getBean("cellularLocationCvRule");
        instance.setBehavior(Behavior.ERROR);     
        CellularLocationVocabulary cl = factory3.createCellularLocationVocabulary();
        cl.addTerm("cytoplasm");
        instance.check(cl);
    }


    @Test(expected=BiopaxValidatorException.class)
    public void testXrefRuleWrong() {
        XrefRule instance =  (XrefRule) context.getBean("xrefRule");
        instance.setBehavior(Behavior.ERROR);
        UnificationXref x = factory3.createUnificationXref();
        x.setDb("LOCATION?");
        instance.check(x);
    }
    

    @Test(expected=BiopaxValidatorException.class)
    public void testXrefRule() {
        XrefRule instance =  (XrefRule) context.getBean("xrefRule");
        instance.setBehavior(Behavior.ERROR);
        UnificationXref x = factory3.createUnificationXref();
        x.setDb("reactome");
        x.setId("0000000");
        instance.check(x);
    }
   
    @Test
    public void testInteractionTypeRule() {
        InteractionTypeCvRule instance =  
                (InteractionTypeCvRule) context.getBean("interactionTypeCvRule");    
        
        Model m = factory3.createModel();
        
        InteractionVocabulary iv = factory3.createInteractionVocabulary();
        iv.addTerm("phosphorylation reaction");
        iv.addComment("Preferred term");
        iv.setRDFId("preferredCVTerm");
        m.add(iv);
        instance.check(iv);
        
        iv = factory3.createInteractionVocabulary();
        iv.addTerm("phosphorylation");
        iv.addComment("Valid term");
        iv.setRDFId("synonymCVTerm");
        m.add(iv);
        instance.check(iv);
        
        iv = factory3.createInteractionVocabulary();
        iv.addTerm("Phosphorylation");
        iv.addComment("Ok term: upper or lower case letters do not matter.");
        iv.setRDFId("okCVTerm");
        m.add(iv);
        instance.check(iv);
        
        iv = factory3.createInteractionVocabulary();
        iv.addTerm("phosphorylated");
        iv.addComment("Invalid term (very similar, however)");
        iv.setRDFId("invalidCVTerm");
        m.add(iv);
        try {
        	instance.check(iv);
        	fail("Must be a BiopaxValidatorException!");
        } catch (BiopaxValidatorException e) {
		}
        
        writeExample("testInteractionTypeRule.owl", m);
    }
    
    
    private void writeExample(String file, Model model) {
    	try {
			exporter.convertToOWL(model, 
					new FileOutputStream(OUTDIR + file));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
    }
}
