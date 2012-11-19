import static org.junit.Assert.*;

import java.io.*;
import java.net.URI;

import org.biopax.paxtools.controller.EditorMap;
import org.biopax.paxtools.controller.SimpleEditorMap;
import org.biopax.paxtools.io.*;
import org.biopax.paxtools.model.*;
import org.biopax.paxtools.model.level3.*;
import org.biopax.validator.api.Rule;
import org.biopax.validator.api.beans.Validation;
import org.biopax.validator.impl.IdentifierImpl;
import org.biopax.validator.rules.*;
import org.junit.Test;


import org.biopax.paxtools.model.level3.Process;


/**
 * This test suite is also generates the examples (BioPAX L3 OWL files)
 * that illustrate the corresponding rule violation.
 * 
 * TODO Test all the L3 rules and generate OWL examples (for invalid cases).
 * TODO Also test valid use cases (look for 'false positives').
 * 
 * @author rodche
 */
public class Level3RulesUnitTest {

	static BioPAXFactory level3 =BioPAXLevel.L3.getDefaultFactory(); // to create BioPAX objects
	static EditorMap editorMap = SimpleEditorMap.L3;
	static BioPAXIOHandler exporter = new SimpleIOHandler(BioPAXLevel.L3);
	
	final static String TEST_DATA_DIR = Level3RulesUnitTest.class
		.getResource("").getPath();
	
	void writeExample(String file, Model model) {
    	try {
			exporter.convertToOWL(model, 
				new FileOutputStream(TEST_DATA_DIR + File.separator + file));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
    }
	
	
	@Test
	public void testBiochemicalPathwayStepProcessOnlyControlCRRule() 
		throws IOException
	{
		Rule rule = new BiochemicalPathwayStepProcessOnlyControlCRRule();	
		BiochemicalPathwayStep step = level3.create(BiochemicalPathwayStep.class, "step1");
		Conversion conv = level3.create(BiochemicalReaction.class, "interaction1");
		conv.addComment("a conversion reaction (not Control)");
		step.addStepProcess((Process) conv);
		step.addComment("error: has not a Control type step process");
		
		Validation v = new Validation(new IdentifierImpl());
		rule.check(v, step); 
		assertFalse(v.getError().isEmpty());
		assertEquals(1, v.countErrors(step.getRDFId(), null, "range.violated", null, false, false));
		
		//System.out.println("thrown: " + e);
		Model m = level3.createModel();
		m.add(conv);
		m.add(step);
		writeExample("testBiochemicalPathwayStepProcessOnlyControlCRRule.owl",m);
	}
	
	
	@Test
	public void testCanCheckBiochemPathwayStepOneConversionRule() {
		Rule rule = new BiochemPathwayStepOneConversionRule();		
		BiochemicalPathwayStep bpstep = level3.create(BiochemicalPathwayStep.class, "1");
		BioPAXElement bpstepElement = level3.create(BiochemicalPathwayStep.class, "2");
		PathwayStep pstep = level3.create(PathwayStep.class, "3");
		// in real data, a subclass of Conversion should be used! 
		BioPAXElement bpe = level3.create(Conversion.class, "4"); 
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
		BiochemicalPathwayStep step = level3.create(BiochemicalPathwayStep.class, "step1");
		step.addComment("error: conversion cannot be a step process (only stepConversion)");
		
		//ok
		Conversion conv = level3.create(BiochemicalReaction.class, "conversion1");
		step.setStepConversion(conv);
		Validation v = new Validation(new IdentifierImpl());
		rule.check(v, step);
		assertTrue(v.getError().isEmpty());
		
		//ok
		Catalysis catalysis = level3.create(Catalysis.class, "catalysis1");
		catalysis.addComment("valid step process value");
		step.addStepProcess((org.biopax.paxtools.model.level3.Process)catalysis); //org.biopax.paxtools.model.level3.Process !!!
		v = new Validation(new IdentifierImpl());
		rule.check(v, step);
		assertTrue(v.getError().isEmpty());
		
		//illegal process
		step.addStepProcess((Process) conv);
		
		v = new Validation(new IdentifierImpl());
		rule.check(v, step); 
		assertFalse(v.getError().isEmpty());
		assertEquals(1, v.countErrors(step.getRDFId(), "org.biopax.validator.rules.BiochemPathwayStepOneConversionRule", 
			"misplaced.step.conversion", null, false, false));
		
		// generate the example OWL
		Model m = level3.createModel();
		m.add(step);
		m.add(conv);
		m.add(catalysis);
		writeExample("testBiochemPathwayStepOneConversionRule.owl",m);
	}

	//InteractionParticipantsLocationRule
	
	@Test
	public void testBiochemReactParticipantsLocationRule() throws IOException {
		Rule rule = new BiochemReactParticipantsLocationRule();
		BiochemicalReaction reaction = level3.create(BiochemicalReaction.class, "#BiochemicalReaction");	
		Dna left = level3.create(Dna.class, "#dna");
		Dna right = level3.create(Dna.class, "#modifiedDna");
		EntityFeature feature = level3.create(FragmentFeature.class, "feature1");
		right.addFeature(feature);
		right.addComment("modified dna");
		
		DnaReference dnaReference = level3.create(DnaReference.class, "#dnaref");
		// set the same type (entity reference)
		left.setEntityReference(dnaReference);
		right.setEntityReference(dnaReference);

		CellularLocationVocabulary cl = level3.create(CellularLocationVocabulary.class, "#cl");
		CellularLocationVocabulary cr = level3.create(CellularLocationVocabulary.class, "#cr");
		cl.addTerm("cytoplasm");
		cr.addTerm("membrane");
		left.addName("dnaLeft");
		right.addName("dnaRight");
		reaction.addLeft(left);
		reaction.addRight(right);
		
		// ok
		Validation v = new Validation(new IdentifierImpl());
		left.setCellularLocation(cl); 
		right.setCellularLocation(cl); 
		rule.check(v, reaction);
		assertTrue(v.getError().isEmpty());
		
		// test complex (rule cannot use entityReference to match "the same" entity)
		PhysicalEntity leftc = level3.create(Complex.class, "#complex");
		PhysicalEntity rightc = level3.create(Complex.class, "#modifiedCmplex");
		leftc.addName("cplx1");
		rightc.addName("cplx1"); 
		rightc.setCellularLocation(cl);
		reaction.addLeft(leftc);
		reaction.addRight(rightc);
		leftc.setCellularLocation(cr);
		leftc.addComment("location changed without transport?");
		
		v = new Validation(new IdentifierImpl());
		rule.check(v, reaction);
		assertEquals(1, v.countErrors(reaction.getRDFId(), null, "participant.location.changed", null, false, false));
		
		// different complex, another location is ok for this rule,
		// but this is another problem (other rule will catch this)
		// for complexes, not ER but names are used to match...
		leftc.removeName("cplx1");
		leftc.addName("cplx2");
		v = new Validation(new IdentifierImpl());
		rule.check(v, reaction);
		assertTrue(v.getError().isEmpty());
		
		right.setCellularLocation(cr); 
		right.addComment("location changed without transport?");
		// check for: same entity (names intersection), diff. location
		v = new Validation(new IdentifierImpl());
		rule.check(v, reaction);
		assertEquals(1, v.countErrors(reaction.getRDFId(), null, "participant.location.changed", null, false, false));

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
		
		//same locations is ok
		leftc.removeName("cplx2");
		leftc.addName("cplx1");
		leftc.setCellularLocation(cr);
		rightc.setCellularLocation(cr);
		right.setCellularLocation(cr); 
		left.setCellularLocation(cr); 
		v = new Validation(new IdentifierImpl());
		rule.check(v, reaction);
		assertTrue(v.getError().isEmpty());
	}
	
	
	@Test
	public void testBiochemReactParticipantsLocationRule_Transport() throws IOException {
		Rule rule = new BiochemReactParticipantsLocationRule();
		BiochemicalReaction reaction = level3.create(TransportWithBiochemicalReaction.class, "#transportWithBiochemicalReaction");
		reaction.addComment("This Transport contains one Rna that did not change its " +
			"cellular location (error!) and another one that did not have any (which is now ok)");
		
		// to generate example
		Model m = level3.createModel();
		m.add(reaction);
		
		Rna left = level3.create(Rna.class, "#Rna1");
		Rna right = level3.create(Rna.class, "#modRna1");
		EntityFeature feature = level3.create(ModificationFeature.class, "feature1");
		right.addFeature(feature);
		right.addComment("modified");
		RnaReference rnaReference = level3.create(RnaReference.class, "#rnaRef");
		// set the same type (entity reference)
		left.setEntityReference(rnaReference);
		right.setEntityReference(rnaReference);
		CellularLocationVocabulary cl = level3.create(CellularLocationVocabulary.class, "#cl");
		CellularLocationVocabulary cr = level3.create(CellularLocationVocabulary.class, "#cr");
		cl.addTerm("nucleus");
		cr.addTerm("cytoplasm");
		left.setDisplayName("rnaLeft1");
		right.setDisplayName("rnaRight1");
		reaction.addLeft(left);
		reaction.addRight(right);

		// set different locations
		left.setCellularLocation(cl); 
		right.setCellularLocation(cr); 
		// make sure it's valid
		Validation v = new Validation(new IdentifierImpl());
		rule.check(v, reaction);
		assertTrue(v.getError().isEmpty());
		
		// now set the same location on both sides and check	
		right.setCellularLocation(cl); 
		v = new Validation(new IdentifierImpl());
		rule.check(v, reaction); 
		assertEquals(1, v.countErrors(reaction.getRDFId(), null, "transport.location.same", null, false, false));
		
		m.add(left); m.add(right);
		m.add(rnaReference);
		m.add(cl); m.add(cr);
		m.add(feature);
		
		// Now check with location is null on one side
		right = level3.create(Rna.class, "#Rna2");
		left = level3.create(Rna.class, "#modRna2");
		left.addFeature(feature);
		left.addComment("modified");
		
		// set the same type (entity reference)
		left.setEntityReference(rnaReference);
		right.setEntityReference(rnaReference);
		left.setDisplayName("rnaLeft2");
		right.setDisplayName("rnaRight2");
		reaction.addLeft(left);
		reaction.addRight(right);
		right.setCellularLocation(cr); 
		m.add(left);
		m.add(right);
		
		// generate the example OWL
		writeExample("testBiochemReactParticipantsLocationRule_Transport.owl", m);

		// fix the error for the first Rna:
		right = (Rna) m.getByID("#modRna1");
		right.setCellularLocation(cr);
		
		// check again
		v = new Validation(new IdentifierImpl());
		rule.check(v, reaction);
		assertTrue(v.getError().isEmpty());
	}
	
	
	
	@Test
	public void testBiopaxElementIdRule() throws IOException {
		Rule<BioPAXElement> rule = new BiopaxElementIdRule();
		Level3Element bpe = level3.create(UnificationXref.class, 
				"http://www.biopax.org/UnificationXref#Taxonomy_40674");
		bpe.addComment("This is a valid ID");
		Validation v = new Validation(new IdentifierImpl());
		rule.check(v, bpe);
		assertTrue(v.getError().isEmpty());
		
		Model m = level3.createModel(); // to later generate examples
		m.add(bpe);
		
		bpe = level3.create(UnificationXref.class, "Taxonomy UnificationXref_40674");
		bpe.addComment("Invalid ID (has a space)");
		v = new Validation(new IdentifierImpl());
		rule.check(v, bpe); 
		assertEquals(1, v.countErrors(v.identify(bpe), null, "invalid.rdf.id", null, false, false));

		m.add(bpe);		
		writeExample("testBiopaxElementIdRule.owl", m);
		
		// weird but legal URIs:
		URI.create("#a");
		URI.create("a,b,c");
		//URI.create("a[b"); // will fail
	}
	
    @Test
    public void testDuplicateNamesByExporter() throws IOException {
    	Protein p = level3.create(Protein.class, "myProtein");
    	String name = "aDisplayName";
    	p.setDisplayName(name);
    	p.addComment("Display Name should not be repeated again in the Name property!");
    	Model m = level3.createModel();
    	m.add(p);
    	writeExample("testDuplicateNamesByExporter.xml", m);
    	
    	// read back and tricky-test
    	BufferedReader in = new BufferedReader(new FileReader(
    			TEST_DATA_DIR + File.separator + "testDuplicateNamesByExporter.xml"));
    	char[] buf = new char[1000];
    	in.read(buf);
    	String xml = new String(buf);
    	if(xml.indexOf(name) != xml.lastIndexOf(name)) {
    		fail("displayName gets duplicated by the SimpleExporter!");
    	}	
    }
    
    
    @Test
    public void testProteinReferenceOrganismRule() throws IOException {
    	ProteinReferenceOrganismRule rule = new ProteinReferenceOrganismRule();
    	BioSource bioSource = level3.create(BioSource.class, "BioSource-Human");
    	bioSource.setDisplayName("Homo sapiens");
    	UnificationXref taxonXref = level3.create(UnificationXref.class, "Taxonomy_UnificationXref_9606");
    	taxonXref.setDb("taxonomy");
    	taxonXref.setId("9606");
    	bioSource.addXref(taxonXref);
    	ProteinReference pr = level3.create(ProteinReference.class, "ProteinReference1");
    	pr.setDisplayName("ProteinReference1");
    	pr.addComment("No value is set for the 'organism' property (must be exactly one)!");
    	
    	Validation v = new Validation(new IdentifierImpl());
    	rule.check(v, pr);
    	assertEquals(1, v.countErrors(pr.getRDFId(), null, "cardinality.violated", null, false, false));

       	// write the example
       	Model m = level3.createModel();
       	m.add(taxonXref);
       	m.add(bioSource);
       	m.add(pr);
       	writeExample("testProteinReferenceOrganismCRRule.owl", m);
    	
    	pr.setOrganism(bioSource);
    	v = new Validation(new IdentifierImpl());
    	rule.check(v, pr); // should pass now
    	assertTrue(v.getError().isEmpty());
    }
    

	@Test
	public void testControlTypeRule() throws IOException
	{
		Rule rule = new ControlTypeRule();	
		Catalysis ca = level3.create(Catalysis.class, "catalysis1");
		Validation v = new Validation(new IdentifierImpl());
		rule.check(v, ca); // controlType==null, no error expected
		assertTrue(v.getError().isEmpty());
		ca.setControlType(ControlType.ACTIVATION);
		v = new Validation(new IdentifierImpl());
		rule.check(v, ca); // no error expected
		assertTrue(v.getError().isEmpty());
		ca.setControlType(ControlType.INHIBITION);
		ca.addComment("error: illegal controlType");
		v = new Validation(new IdentifierImpl());
		rule.check(v, ca); 
		assertEquals(1, v.countErrors(ca.getRDFId(), null, "range.violated", null, false, false));
		
		TemplateReactionRegulation tr = level3.create(TemplateReactionRegulation.class, "regulation1");
		tr.setControlType(ControlType.INHIBITION);
		v = new Validation(new IdentifierImpl());
		rule.check(v, tr); // no error...
		assertTrue(v.getError().isEmpty());
		tr.setControlType(ControlType.ACTIVATION_ALLOSTERIC);
		tr.addComment("error: illegal controlType");
		v = new Validation(new IdentifierImpl());
		rule.check(v, tr); 
		assertEquals(1, v.countErrors(tr.getRDFId(), null, "range.violated", null, false, false));
		
		// write the example XML
		Model m = level3.createModel();
		ca.setControlType(ControlType.INHIBITION); // set wrong
		tr.setControlType(ControlType.ACTIVATION_ALLOSTERIC); // set bad
		m.add(ca);
		m.add(tr);
		writeExample("testControlTypeRule.owl",m);
	}
	
    
	@Test
	public void testDegradationConversionDirectionRule() throws IOException
	{
		Rule rule = new DegradationConversionDirectionRule();
		Conversion dg = level3.create(Degradation.class, "degradation-conversion-1");
		Validation v = new Validation(new IdentifierImpl());
		rule.check(v, dg); // direction is null, no error
		assertTrue(v.getError().isEmpty());
		dg.setConversionDirection(ConversionDirectionType.REVERSIBLE);
		dg.addComment("error: illegal conversionDirection");
		v = new Validation(new IdentifierImpl());
		rule.check(v, dg); 
		assertEquals(1, v.countErrors(dg.getRDFId(), null, "range.violated", null, false, false));
		
		// write the example
		Model m = level3.createModel();
		dg.setConversionDirection(ConversionDirectionType.REVERSIBLE);
		m.add(dg);
		writeExample("testDegradationConversionDirectionRule.owl",m);		
	}
    
	/* Test EntityReferenceSamePhysicalEntitiesRule
	 *
<bp:Protein rdf:ID="pid_181">
  <bp:entityReference rdf:resource="#pid_182"/>
  <bp:name rdf:datatype="http://www.w3.org/2001/XMLSchema#string">Rac1</bp:name>
  <bp:name rdf:datatype="http://www.w3.org/2001/XMLSchema#string">RAC1</bp:name>
</bp:Protein>
<bp:Protein rdf:ID="pid_305">
  <bp:entityReference rdf:resource="#pid_182"/>
  <bp:name rdf:datatype="http://www.w3.org/2001/XMLSchema#string">Rac1</bp:name>
  <bp:name rdf:datatype="http://www.w3.org/2001/XMLSchema#string">RAC1</bp:name>
  <bp:cellularLocation rdf:resource="#pid_192"/>
</bp:Protein>
	 *
	 */
	@Test
	public void testEntityReferenceSamePhysicalEntitiesRule() {
		Model model = BioPAXLevel.L3.getDefaultFactory().createModel();
	   	ProteinReference pr = model.addNew(ProteinReference.class, "pid_182");
    	pr.setDisplayName("ProteinReference");
    	CellularLocationVocabulary cv = model.addNew(CellularLocationVocabulary.class, "pid_192");
    	cv.addTerm("plasma membrane");
		Protein p = model.addNew(Protein.class, "pid_181");
		p.setEntityReference(pr);
		p.addName("Rac1");
		p = model.addNew(Protein.class, "pid_305");
		p.setEntityReference(pr);
		p.addName("Rac1");
    	p.setCellularLocation(cv);
		
    	Rule<EntityReference> rule = new EntityReferenceSamePhysicalEntitiesRule();
    	Validation v = new Validation(new IdentifierImpl());
    	rule.check(v, pr);
    	// no err: different location
    	assertTrue(v.getError().isEmpty());
	}
	
	@Test
	public final void testClonedUtilityClassRule() {
		Model model = BioPAXLevel.L3.getDefaultFactory().createModel();
    	Xref ref = model.addNew(UnificationXref.class,"Xref1");
    	ref.setDb("uniprotkb"); // normalizer should convert this to 'uniprot'
    	ref.setId("P68250");
    	ProteinReference pr = model.addNew(ProteinReference.class, "ProteinReference1");
    	pr.setDisplayName("ProteinReference1");
    	pr.addXref(ref);
    	ref = model.addNew(RelationshipXref.class, "Xref2");
    	ref.setDb("refseq");
    	ref.setId("NP_001734");
    	ref.setIdVersion("1");  // this xref won't be removed by norm. (version matters in xrefs comparing!)
		pr.addXref(ref);
	   	ref = model.addNew(UnificationXref.class, "Xref3");
    	ref.setDb("uniprotkb");
    	ref.setId("Q0VCL1"); 
    	Xref uniprotX = ref;
    	
    	pr = model.addNew(ProteinReference.class, "ProteinReference2");
    	pr.setDisplayName("ProteinReference2");
    	pr.addXref(uniprotX);
    	ref = model.addNew(RelationshipXref.class, "Xref4");
    	ref.setDb("refseq");
    	ref.setId("NP_001734");
		pr.addXref(ref);
		
		// this ER is duplicate (same uniprot xref as ProteinReference2's) and must be removed by normalizer
    	pr = model.addNew(ProteinReference.class, "ProteinReference3");
    	pr.setDisplayName("ProteinReference3");
    	pr.addXref(uniprotX);
    	ref = model.addNew(RelationshipXref.class, "Xref5");
    	ref.setDb("refseq");
    	ref.setId("NP_001734");
		pr.addXref(ref);
		
		Rule rule = new ClonedUtilityClassRule();
		assertTrue(rule.canCheck(model));
		
		// check
		Validation v = new Validation(new IdentifierImpl());
		rule.check(v, model); 
		assertEquals(1, v.countErrors(null, null, "cloned.utility.class", null, false, false));

		// write the example
		writeExample("testClonedUtilityClassRule.owl", model);	
		
		// now -fix!
		v = new Validation(new IdentifierImpl(), "auto-fix-it", true, null, 0, null);
		rule.check(v, model);
		//there is one error case, but -
		assertEquals(1, v.countErrors(null, null, "cloned.utility.class", null, false, false));
		// - it is fixed
		assertEquals(0, v.countErrors(null, null, "cloned.utility.class", null, false, true));
		// write the example
		writeExample("testClonedUtilityClassRuleFixed.owl", model);	
	}

	
	@Test
	public void testDuplicateIdCaseInsensitiveRule() {
		Rule<Model> rule = new DuplicateIdCaseInsensitiveRule();		
		Model m = level3.createModel();
		m.addNew(UnificationXref.class, "some_id");
		m.addNew(RelationshipXref.class, "Some_ID");
		Validation v = new Validation(new IdentifierImpl()); 
		rule.check(v, m); 
		assertEquals(1, v.countErrors(null, null, "duplicate.id.ignoringcase", null, false, false));
	}

	
    @Test
    public void testConversionStoichiometryCheckRule() {
        Rule<Conversion> rule = new ConversionStoichiometryCheckRule();
        Model m = level3.createModel();

        Conversion c1 = m.addNew(Degradation.class, "degradation");
        Conversion c2 = m.addNew(BiochemicalReaction.class, "biochem_reaction");

        PhysicalEntity p1 = m.addNew(Protein.class, "protein1");
        PhysicalEntity p2 = m.addNew(Protein.class, "protein2");
        Complex complex = m.addNew(Complex.class, "complex");

        Stoichiometry s1 = m.addNew(Stoichiometry.class, "stoi1");
        Stoichiometry s2 = m.addNew(Stoichiometry.class, "stoi2");

        s1.setPhysicalEntity(p1);
        s1.setStoichiometricCoefficient(2.0f);

        s2.setPhysicalEntity(p1);
        s2.setStoichiometricCoefficient(1.0f);

        complex.addComponent(p1);
        complex.addComponentStoichiometry(s1);
        complex.addComponent(p2);

        c1.addLeft(p1);
        c1.addLeft(p2);
        c1.addRight(complex);
        c1.addParticipantStoichiometry(s2);

        c2.addLeft(p1);
        c2.addLeft(p2);
        c2.addRight(complex);
        c2.addParticipantStoichiometry(s2);

        assertFalse(rule.canCheck(c1));
        assertTrue(rule.canCheck(c2));

        Validation v = new Validation(new IdentifierImpl());
		rule.check(v, c2);
		assertEquals(1, v.countErrors("biochem_reaction", null, "stoichiometry.mismatch", null, false, false));

        writeExample("testConversionStoichiometryCheckRule.owl", m);
    }

    @Test
    public void testConversionToComplexAssemblyRule() {
        Rule<Conversion> rule = new ConversionToComplexAssemblyRule();
        Model m = level3.createModel();

        Conversion c1 = m.addNew(ComplexAssembly.class, "complex_assembly");
        Conversion c2 = m.addNew(BiochemicalReaction.class, "biochem_reaction");

        PhysicalEntity p1 = m.addNew(Protein.class, "protein1");
        PhysicalEntity p2 = m.addNew(Protein.class, "protein2");
        Complex complex = m.addNew(Complex.class, "complex");

        complex.addComponent(p1);
        complex.addComponent(p2);

        c1.addLeft(p1);
        c1.addLeft(p2);
        c1.addRight(complex);

        c2.addLeft(p1);
        c2.addLeft(p2);
        c2.addRight(complex);

        assertFalse(rule.canCheck(c1));
        assertTrue(rule.canCheck(c2));

        Validation v = new Validation(new IdentifierImpl());
		rule.check(v, c2);
		assertEquals(1, v.countErrors(c2.getRDFId(), null, "wrong.conversion.class", null, false, false));

        writeExample("testConversionToComplexAssemblyRule.owl", m);
    }

    @Test
    public void testPhysicalEntityAmbiguousFeatureRule() {
        Rule<PhysicalEntity> rule = new PhysicalEntityAmbiguousFeatureRule();
        Model m = level3.createModel();
        Conversion c = m.addNew(Degradation.class, "degradation");
        PhysicalEntity p1 = m.addNew(Protein.class, "protein1");
        PhysicalEntity p2 = m.addNew(Protein.class, "protein2");
        Complex complex = m.addNew(Complex.class, "complex");

        complex.addComponent(p1);
        complex.addComponent(p2);
        c.addLeft(p1);

        Validation v = new Validation(new IdentifierImpl());
		rule.check(v, p1);
		assertEquals(1, v.countErrors(p1.getRDFId(), null, "ambiguous.feature", null, false, false));

        writeExample("testPhysicalEntityAmbiguousFeatureRule.owl", m);
    }

    
    @Test
    public void testSimplePhysicalEntityFeaturesRule() {
        Rule<SimplePhysicalEntity> rule = new SimplePhysicalEntityFeaturesRule();
        Model m = level3.createModel();
        SimplePhysicalEntity p = m.addNew(Protein.class, "protein1");
        EntityReference pr = m.addNew(ProteinReference.class, "proteinreference1");
        EntityFeature ef = m.addNew(ModificationFeature.class, "modfeature1");
        EntityFeature ef2 = m.addNew(FragmentFeature.class, "modnotfeature1");

        p.setEntityReference(pr);
        p.addFeature(ef);
        p.addNotFeature(ef2);

        Validation v = new Validation(new IdentifierImpl());
		rule.check(v, p);
		assertEquals(1, v.countErrors(p.getRDFId(), null, "improper.feature.use", null, false, false));

        writeExample("testSimplePhysicalEntityFeaturesRule.owl", m);

        v = new Validation(new IdentifierImpl(),"",true,null,0, null);
        rule.check(v, p);
		assertEquals(1, v.countErrors(p.getRDFId(), null, "improper.feature.use", null, false, false));
		assertEquals(0, v.countErrors(p.getRDFId(), null, "improper.feature.use", null, false, true));
        
        writeExample("testSimplePhysicalEntityFeaturesRuleFixed.owl", m);
    }
    
    
    @Test
    public void testAcyclicComplexRule() {
        Rule<Complex> rule = new AcyclicComplexRule();
        Model m = level3.createModel();

        Complex complex = m.addNew(Complex.class, "complex");
        Complex component = m.addNew(Complex.class, "component");

        // loop
        complex.addComponent(component);
        component.addComponent(complex);

        assertTrue(rule.canCheck(complex));

        Validation v = new Validation(new IdentifierImpl()); //default is: no auto-fix 
		rule.check(v, complex);
		assertEquals(1, v.countErrors(complex.getRDFId(), null, "cyclic.inclusion", null, false, true));
//			System.out.println(e + " " + Arrays.toString(e.getMsgArgs()));

        writeExample("testAcyclicComplexRule.owl", m);
    }

    
    @Test
    public void testSharedUnificationXrefRule() {
        Rule<UnificationXref> rule = new SharedUnificationXrefRule();
        Model m = level3.createModel();
        Evidence ev1 = m.addNew(Evidence.class, "evidence1");
        Evidence ev2 = m.addNew(Evidence.class, "evidence2");
        UnificationXref x = m.addNew(UnificationXref.class, "shared");
        UnificationXref ux1 = m.addNew(UnificationXref.class, "unique1");
        UnificationXref ux2 = m.addNew(UnificationXref.class, "unique2");
        ev1.addXref(x);
        ev1.addXref(ux1);
        ev2.addXref(x);
        ev2.addXref(ux2);

        Validation v = new Validation(new IdentifierImpl());
		rule.check(v, x);
		assertEquals(1, v.countErrors(v.identify(x), null, "shared.unification.xref", null, false, true));

        writeExample("testSharedUnificationXrefRule.owl", m);       
    }
}
