/*
 * #%L
 * BioPAX Validator Integration Tests
 * %%
 * Copyright (C) 2008 - 2013 University of Toronto (baderlab.org) and Memorial Sloan-Kettering Cancer Center (cbio.mskcc.org)
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */
import static org.junit.Assert.*;

import java.util.*;

import org.junit.*;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.BioPAXFactory;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.*;
import org.biopax.validator.api.beans.Validation;
import org.biopax.validator.impl.IdentifierImpl;
import org.biopax.validator.rules.CellularLocationCvRule;
import org.biopax.validator.rules.InteractionTypeCvRule;
import org.biopax.validator.rules.ProteinModificationFeatureCvRule;
import org.biopax.validator.rules.XrefRule;
import org.biopax.validator.rules.XrefSynonymDbRule;
import org.biopax.validator.utils.XrefHelper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author rodche
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:META-INF/spring/appContext-validator.xml"}) // AspectJ LTW is disabled!
public class IntegrationTest {    
    @Autowired
    XrefHelper xrefHelper;
    
    @Autowired
    ApplicationContext context;
    
    BioPAXFactory factory3;
	SimpleIOHandler simpleIO; // to write OWL examples of what rule checks
	final static String OUTDIR = IntegrationTest.class.getResource("").getPath();
	
    
    @Before
    public void setUp() {
        factory3 = BioPAXLevel.L3.getDefaultFactory();
        simpleIO = new SimpleIOHandler(BioPAXLevel.L3);
    }
	   

    @Test
    public void testBuildPaxtoolsL2ModelSimple() throws FileNotFoundException  {
        System.out.println("with Level2 data");
        InputStream is = getClass().getResourceAsStream("biopax_id_557861_mTor_signaling.owl");
        SimpleIOHandler io = new SimpleIOHandler();
        io.mergeDuplicates(true);
        Model model = io.convertFromOWL(is);
        assertNotNull(model);
        assertFalse(model.getObjects().isEmpty());
    }

   
    @Test
    public void testBuildPaxtoolsL3ModelSimple() throws FileNotFoundException {
        System.out.println("with Level3 data");
        InputStream is = getClass().getResourceAsStream("biopax3-short-metabolic-pathway.owl");
        SimpleIOHandler simpleReader = new SimpleIOHandler();
        simpleReader.mergeDuplicates(true);
        Model model = simpleReader.convertFromOWL(is);
        assertNotNull(model);
        assertFalse(model.getObjects().isEmpty());
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
    	x3.setId("doesn't matter");
    	assertFalse(x1.isEquivalent(x3)); // same ID does not matter anymore (since Apr'2011)!
    	
    	x3.setDb("db");
    	x3.setId("id");
    	assertTrue(x1.isEquivalent(x3)); 
    	
    	x3 = x1;
    	assertTrue(x1.isEquivalent(x3)); 
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
    	List<String> gs = xrefHelper.getSynonymsForDbName("go"); 	
    	assertTrue(gs.contains("GO"));
    	assertTrue(gs.contains("GENE ONTOLOGY"));
    	assertTrue(gs.contains("GENE_ONTOLOGY"));
    	
    	assertTrue(xrefHelper.isUnofficialOrMisspelledDbName("GENE_ONTOLOGY"));
    	assertFalse(xrefHelper.isUnofficialOrMisspelledDbName("GO"));
    	assertTrue(xrefHelper.isUnofficialOrMisspelledDbName("medline"));
    }

    
    
    @Test
    public void testXRefHelperContainsSynonyms() {
    	assertNotNull(xrefHelper.getPrimaryDbName("GO"));
    	assertNotNull(xrefHelper.getPrimaryDbName("GENE ONTOLOGY"));
    	assertNotNull(xrefHelper.getPrimaryDbName("GENE_ONTOLOGY"));
    }
    
    @Test
    public void testPrimarySynonym() {
    	//not in Miriam: PIR
    	assertEquals("UNIPROT KNOWLEDGEBASE", xrefHelper.getSynonymsForDbName("pir").get(0));
    	//Miriam: Gene Ontology
    	assertEquals("GENE ONTOLOGY", xrefHelper.getSynonymsForDbName("go").get(0));
    }
    
    @Test
    public void testHasRegexp() {
    	List<String> goes = xrefHelper.getSynonymsForDbName("go");
    	for(String db: goes) {
    		assertEquals("^GO:\\d{7}$", xrefHelper.getRegexpString(db));
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
        Validation v = new Validation(new IdentifierImpl());
		instance.check(v, lcv);
		assertEquals(1, v.countErrors(lcv.getRDFId(), null, null, null, false, false));
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
    	
    	Validation v = new Validation(new IdentifierImpl());
        instance.check(v, cl);
        assertTrue(v.getError().isEmpty());
    }


    @Test
    public void testXrefRuleWrong() {
        XrefRule instance =  (XrefRule) context.getBean("xrefRule");
        UnificationXref x = factory3.create(UnificationXref.class, "1");
        x.setDb("ILLEGAL DB NAME");
        Validation v = new Validation(new IdentifierImpl());
		instance.check(v,x);
		assertEquals(1, v.countErrors(x.getRDFId(), null, "unknown.db", null, false, false));
		
        x.setDb("NCBI"); //ambiguous
        v = new Validation(new IdentifierImpl());
		instance.check(v, x);
		assertEquals(1, v.countErrors(x.getRDFId(), null, "unknown.db", null, false, false));
    }
    

    @Test
    public void testXrefRule() {
        XrefRule instance =  (XrefRule) context.getBean("xrefRule");
        UnificationXref x = factory3.create(UnificationXref.class, "1");
        x.setDb("reactome");
        x.setId("0000000");
        Validation v = new Validation(new IdentifierImpl());
		instance.check(v, x);
		assertEquals(1, v.countErrors(x.getRDFId(), null, "invalid.id.format", null, false, false));
    }
    
    /*
     * Special case - check synonyms are there
     */
    @Test
    public void testXrefRuleEntezGene() {
        XrefRule instance =  (XrefRule) context.getBean("xrefRule");
        UnificationXref x = factory3.create(UnificationXref.class, "1");
        x.setDb("EntrezGene");
        x.setId("0000000");
        Validation v = new Validation(new IdentifierImpl());
        instance.check(v, x);
        assertTrue(v.getError().isEmpty());
    }
    
   
    @Test
    public void testInteractionTypeRule() {
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
        
        Validation v = new Validation(new IdentifierImpl());
        instance.check(v, iv);
        assertTrue(v.getError().isEmpty());
        
        iv = factory3.create(InteractionVocabulary.class, "synonymCVTerm");
        iv.addTerm("phosphorylation");
        iv.addComment("Valid term");
        m.add(iv);
        iv.addXref(ux);
        
        v = new Validation(new IdentifierImpl());
        instance.check(v, iv);
        assertTrue(v.getError().isEmpty());
        
        iv = factory3.create(InteractionVocabulary.class, "okCVTerm");
        iv.addTerm("Phosphorylation");
        iv.addComment("Ok term: upper or lower case letters do not matter.");
        m.add(iv);
        iv.addXref(ux);
        
        v = new Validation(new IdentifierImpl());
        instance.check(v, iv);
        assertTrue(v.getError().isEmpty());
        
        iv = factory3.create(InteractionVocabulary.class, "invalidCVTerm");
        iv.addTerm("phosphorylated residue");
        iv.addComment("Invalid term (very similar, however)");
        ux = factory3.create(UnificationXref.class, "UnificationXref_MOD_00696");
    	ux.setDb("MOD");
    	ux.setId("MOD:00696"); 
    	/* Note: in fact, both MOD:00696 and MI:0217 have synonym name "Phosphorylation"!
    		TODO Validator (currently) checks names in the CV 'term' property only, but also should check what can be inferred from the xref.id!
    	*/
    	iv.addXref(ux);
    	m.add(ux);
        m.add(iv);
        
        v = new Validation(new IdentifierImpl());
		instance.check(v, iv);
		assertEquals(1, v.countErrors(iv.getRDFId(), null, "illegal.cv.term", null, false, false));
        
        writeExample("testInteractionTypeRule.owl", m);
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
    	
    	Validation v = new Validation(new IdentifierImpl());
   		rule.check(v, mf); // should not fail
   		assertTrue(v.getError().isEmpty());
	}
    
    
    @Test
    public void testXrefSynonymDbRule() {
    	XrefSynonymDbRule instance =  (XrefSynonymDbRule) context.getBean("xrefSynonymDbRule");
//        instance.setBehavior(Behavior.ERROR);
        UnificationXref x = factory3.create(UnificationXref.class, "1");
        //use an unofficial/misspelled name
        x.setDb("entrez_gene");
        x.setId("0000000");
        Validation v = new Validation(new IdentifierImpl());
		instance.check(v, x);
		assertEquals(1, v.countErrors(x.getRDFId(), null, "db.name.spelling", null, false, false));
		
        // use one of its official synonyms
        x.setDb("entre-zgene");
        x.setId("0000000");
        v = new Validation(new IdentifierImpl());
        instance.check(v, x);
        assertTrue(v.getError().isEmpty());
        
        //use an unofficial/misspelled name
        x.setDb("gene_ontology");
        x.setId("0000000");
        v = new Validation(new IdentifierImpl());
		instance.check(v, x);
		assertEquals(1, v.countErrors(x.getRDFId(), null, null, null, false, false));
        // use one of its official synonyms
        x.setDb("go");
        x.setId("0000000");
        v = new Validation(new IdentifierImpl());
        instance.check(v, x);
        assertTrue(v.getError().isEmpty());
    }

    
    private void writeExample(String file, Model model) {
    	try {
			simpleIO.convertToOWL(model, 
					new FileOutputStream(OUTDIR + File.separator + file));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
    }
}
