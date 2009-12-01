package tests;

import static org.junit.Assert.*;

import java.io.*;

import org.biopax.paxtools.impl.level3.Level3FactoryImpl;
import org.biopax.paxtools.io.simpleIO.SimpleExporter;
import org.biopax.paxtools.model.*;
import org.biopax.paxtools.model.level3.*;
import org.biopax.validator.Rule;
import org.biopax.validator.rules.*;
import org.biopax.validator.utils.*;
import org.junit.Before;
import org.junit.Test;


import org.biopax.paxtools.model.level3.Process;


/**
 * This test suite is also generates the examples (BioPAX L3 OWL files)
 * that illustrate the corresponding rule violation.
 * 
 * TODO Test all the L3 rules and generate OWL examples (for invalid cases).
 * TODO Test not only wrong but also valid use cases.
 * 
 * @author rodch
 */
public class Level3RulesUnitTest {

	Level3Factory level3; // to create BioPAX objects
	SimpleExporter exporter; // to write OWL examples of what rule checks
	final static String OUTDIR = "target";
	
	@Before
	public void setUp() {
		level3 = new Level3FactoryImpl();
		exporter = new SimpleExporter(BioPAXLevel.L3);
	}
	
	@Test
	public void testBiochemicalPathwayStepProcessOnlyControlCRRule() 
		throws IOException
	{
		Rule rule = new BiochemicalPathwayStepProcessOnlyControlCRRule();	
		BiochemicalPathwayStep step = level3.createBiochemicalPathwayStep();
		step.setRDFId("step1");
		Conversion conv = level3.createBiochemicalReaction();
		conv.setRDFId("interaction1");
		conv.addComment("a conversion reaction (not Control)");
		step.addStepProcess((Process) conv);
		step.addComment("error: has not a Control type step process");
		try {
			rule.check(step); 
			fail("must throw BiopaxValidatorException");
		} catch(BiopaxValidatorException e) {
			Model m = level3.createModel();
			m.add(conv);
			m.add(step);
			writeExample("testBiochemicalPathwayStepProcessOnlyControlCRRule.owl",m);
		}
		
	}
	
	
	@Test
	public void testCanCheckBiochemPathwayStepOneConversionRule() {
		Rule rule = new BiochemPathwayStepOneConversionRule();		
		BiochemicalPathwayStep bpstep = level3.createBiochemicalPathwayStep();
		BioPAXElement bpstepElement = level3.createBiochemicalPathwayStep();
		PathwayStep pstep = level3.createPathwayStep();
		BioPAXElement bpe = level3.createConversion(); // in real data, a subclass of Conversion should be used! 
		assertFalse(rule.canCheck(null));
		assertFalse(rule.canCheck(new Object()));
		assertFalse(rule.canCheck(pstep));
		assertFalse(rule.canCheck(bpe));
		assertTrue(rule.canCheck(bpstep));
		assertTrue(rule.canCheck(bpstepElement));
	}

	
	@Test
	public void testBiochemPathwayStepOneConversionRule() throws IOException {
		Rule rule = new BiochemPathwayStepOneConversionRule();	
		BiochemicalPathwayStep step = level3.createBiochemicalPathwayStep();
		step.setRDFId("step1");
		step.addComment("error: conversion cannot be a step process (only stepConversion)");
		
		//ok
		Conversion conv = level3.createBiochemicalReaction();
		conv.setRDFId("conversion1");
		step.setStepConversion(conv);
		rule.check(step); // shouldn't throw a BiopaxValidatorException
		
		//ok
		Catalysis catalysis = level3.createCatalysis();
		catalysis.setRDFId("catalysis1");
		catalysis.addComment("valid step process value");
		step.addStepProcess((Process)catalysis); //org.biopax.paxtools.model.level3.Process !!! :)
		rule.check(step);
		
		//illegal process
		step.addStepProcess((Process) conv);
		try {
			rule.check(step); 
			fail("must throw BiopaxValidatorException");
		} catch(BiopaxValidatorException e) {
			// generate the example OWL
			Model m = level3.createModel();
			m.add(step);
			m.add(conv);
			m.add(catalysis);
			writeExample("testBiochemPathwayStepOneConversionRule.owl",m);
		}
	}

	//InteractionParticipantsLocationRule
	
	@Test
	public void testBiochemReactParticipantsLocationRule() throws IOException {
		Rule rule = new BiochemReactParticipantsLocationRule();
		BiochemicalReaction reaction = level3.createBiochemicalReaction();	
		reaction.setRDFId("#BiochemicalReaction");
		Dna left = level3.createDna(); left.setRDFId("#dna");
		Dna right = level3.createDna(); right.setRDFId("#modifiedDna");
		EntityFeature feature = level3.createFragmentFeature();
		feature.setRDFId("feature1");
		right.addFeature(feature);
		right.addComment("modified dna");
		
		DnaReference dnaReference = level3.createDnaReference();
		dnaReference.setRDFId("#dnaref");
		// set the same type (entity reference)
		left.setEntityReference(dnaReference);
		right.setEntityReference(dnaReference);

		CellularLocationVocabulary cl = level3.createCellularLocationVocabulary();
		CellularLocationVocabulary cr = level3.createCellularLocationVocabulary();
		cl.setRDFId("#cl"); cl.addTerm("cytoplasm");
		cr.setRDFId("#cr"); cr.addTerm("membrane");
		left.addName("dnaLeft");
		right.addName("dnaRight");
		reaction.addLeft(left);
		reaction.addRight(right);
		
		// ok
		left.setCellularLocation(cl); 
		right.setCellularLocation(cl); 
		rule.check(reaction);
		
		// test complex (rule cannot use entityReference to match "the same" entity)
		PhysicalEntity leftc = level3.createComplex(); 
		leftc.setRDFId("#complex");
		PhysicalEntity rightc = level3.createComplex(); 
		rightc.setRDFId("#modifiedCmplex");
		leftc.addName("cplx1");
		rightc.addName("cplx1"); 
		rightc.setCellularLocation(cl);
		reaction.addLeft(leftc);
		reaction.addRight(rightc);
		leftc.setCellularLocation(cr);
		leftc.addComment("location changed without transport?");
		try {
			rule.check(reaction);
			fail("must throw BiopaxValidatorException");
		} catch (BiopaxValidatorException e) {
		}
		
		// different complex, another location is ok for this rule,
		// but this is another problem (other rule will catch this)
		// for complexes, not ER but names are used to match...
		leftc.removeName("cplx1");
		leftc.addName("cplx2");
		rule.check(reaction);
		
		right.setCellularLocation(cr); 
		right.addComment("location changed without transport?");
		// check for: same entity (names intersection), diff. location
		try {
			rule.check(reaction); 
			fail("must throw BiopaxValidatorException");
		} catch(BiopaxValidatorException e) {
			// generate the example OWL
			Model m = level3.createModel();
			m.add(reaction);
			m.add(left); m.add(right);
			m.add(dnaReference);
			m.add(cl); m.add(cr);
			m.add(feature);
			m.add(leftc);
			m.add(rightc);
			writeExample("testBiochemReactParticipantsLocationRule.owl",m);
		}
		
		//same locations is ok
		leftc.removeName("cplx2");
		leftc.addName("cplx1");
		leftc.setCellularLocation(cr);
		rightc.setCellularLocation(cr);
		right.setCellularLocation(cr); 
		left.setCellularLocation(cr); 
		rule.check(reaction);
	}
	
	
	@Test
	public void testBiochemReactParticipantsLocationRule_Transport() throws IOException {
		Rule rule = new BiochemReactParticipantsLocationRule();
		BiochemicalReaction reaction = level3.createTransportWithBiochemicalReaction();	
		reaction.setRDFId("#transportWithBiochemicalReaction");
		Rna left = level3.createRna(); left.setRDFId("#rna");
		Rna right = level3.createRna(); right.setRDFId("#movedRna");
		EntityFeature feature = level3.createFragmentFeature();
		feature.setRDFId("feature1");
		right.addFeature(feature);
		right.addComment("modified");
		
		RnaReference rnaReference = level3.createRnaReference();
		rnaReference.setRDFId("#rnaref");
		// set the same type (entity reference)
		left.setEntityReference(rnaReference);
		right.setEntityReference(rnaReference);

		CellularLocationVocabulary cl = level3.createCellularLocationVocabulary();
		CellularLocationVocabulary cr = level3.createCellularLocationVocabulary();
		cl.setRDFId("#cl"); cl.addTerm("cytoplasm");
		cr.setRDFId("#cr"); cr.addTerm("membrane");
		left.addName("rnaLeft");
		right.addName("rnaRight");
		reaction.addLeft(left);
		reaction.addRight(right);
		
		// ok
		left.setCellularLocation(cl); 
		right.setCellularLocation(cr); 
		rule.check(reaction);
		
		// nok	
		right.setCellularLocation(cl); 
		right.addComment("location not changed?");
		// check for: same entity (names intersection), diff. location
		try {
			rule.check(reaction); 
			fail("must throw BiopaxValidatorException");
		} catch(BiopaxValidatorException e) {
			// generate the example OWL
			Model m = level3.createModel();
			m.add(reaction);
			m.add(left); m.add(right);
			m.add(rnaReference);
			m.add(cl); m.add(cr);
			m.add(feature);
			writeExample("testBiochemReactParticipantsLocationRule_Transport.owl", m);
		}
		
	}
	
	
	
	@Test
	public void testBiopaxElementIdRule() throws IOException {
		Rule<BioPAXElement> rule = new BiopaxElementIdRule();
		Level3Element bpe = level3.createUnificationXref();
		bpe.setRDFId("Taxonomy_UnificationXref_40674");
		bpe.addComment("This is a valid ID");
		rule.check(bpe);
		
		Model m = level3.createModel(); // to later generate examples
		m.add(bpe);
		
		bpe = level3.createUnificationXref();
		bpe.setRDFId("Taxonomy UnificationXref_40674");
		bpe.addComment("Invalid ID (has a space)");
		try { 
			rule.check(bpe); 
			fail("must throw BiopaxValidatorException");
		} catch (BiopaxValidatorException e) 
		{
			m.add(bpe);
		}
		
		bpe = level3.createUnificationXref();
		bpe.setRDFId("Taxonomy:40674");
		bpe.addComment("Invalid ID (contains a colon)");
		try { 
			rule.check(bpe); 
			fail("must throw BiopaxValidatorException");
		} catch (BiopaxValidatorException e) 
		{
			m.add(bpe);
		}
		
		bpe = level3.createUnificationXref();
		bpe.setRDFId("123_Taxonomy");
		bpe.addComment("Invalid ID (starts with a digit)");
		try { 
			rule.check(bpe); 
			fail("must throw BiopaxValidatorException");
		} catch (BiopaxValidatorException e) 
		{
			m.add(bpe);
		}
		
		writeExample("testBiopaxElementIdRule.owl", m);
		
	}
	
    private void writeExample(String file, Model model) {
    	try {
			exporter.convertToOWL(model, 
					new FileOutputStream(OUTDIR + file));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
    }
	
    
    //@Test
    public void testDuplicateNamesByExporter() throws IOException {
    	Protein p = level3.createProtein();
    	String name = "aDisplayName";
    	p.setRDFId("myProtein");
    	p.setDisplayName(name);
    	p.addComment("Display Name should not be repeated again in the Name property!");
    	Model m = level3.createModel();
    	m.add(p);
    	writeExample("testDuplicateNamesByExporter.xml", m);
    	BufferedReader in = new BufferedReader(new FileReader(
    			OUTDIR + "testDuplicateNamesByExporter.xml"));
    	char[] buf = new char[1000];
    	in.read(buf);
    	String xml = new String(buf);
    	if(xml.indexOf(name) != xml.lastIndexOf(name)) {
    		fail("displayName gets duplicated by the SimpleExporter!");
    	}
    	
    }
    
}
