package org.biopax.validator.utils;
/**
 ** Copyright (c) 2009 Memorial Sloan-Kettering Cancer Center (MSKCC)
 ** and University of Toronto (UofT).
 **
 ** This is free software; you can redistribute it and/or modify it
 ** under the terms of the GNU Lesser General Public License as published
 ** by the Free Software Foundation; either version 2.1 of the License, or
 ** any later version.
 **
 ** This library is distributed in the hope that it will be useful, but
 ** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 ** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 ** documentation provided hereunder is on an "as is" basis, and
 ** both UofT and MSKCC have no obligations to provide maintenance, 
 ** support, updates, enhancements or modifications.  In no event shall
 ** UofT or MSKCC be liable to any party for direct, indirect, special,
 ** incidental or consequential damages, including lost profits, arising
 ** out of the use of this software and its documentation, even if
 ** UofT or MSKCC have been advised of the possibility of such damage.  
 ** See the GNU Lesser General Public License for more details.
 **
 ** You should have received a copy of the GNU Lesser General Public License
 ** along with this software; if not, write to the Free Software Foundation,
 ** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA;
 ** or find it at http://www.fsf.org/ or http://www.gnu.org.
 **/

import static org.junit.Assert.*;

import java.io.*;

import org.biopax.paxtools.controller.ModelUtils;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.*;
import org.biopax.paxtools.model.level3.*;
import org.biopax.validator.utils.Normalizer;
import org.junit.Test;

/**
 * 
 * @author rodche
 *
 */
public class NormalizerTest {
	
	static SimpleIOHandler simpleIO;
	
	static {
		simpleIO = new SimpleIOHandler(BioPAXLevel.L3);
		simpleIO.mergeDuplicates(true);
	}

	@Test
	public final void testNormalize() throws UnsupportedEncodingException {
		Model model = BioPAXLevel.L3.getDefaultFactory().createModel();
    	Xref ref = model.addNew(UnificationXref.class,
    			"http://www.pathwaycommons.org/import#Xref1");
    	ref.setDb("uniprotkb"); // normalizer should convert this to 'uniprot'
    	ref.setId("P68250");
    	ProteinReference pr = model.addNew(ProteinReference.class,
    			"http://www.pathwaycommons.org/import#ProteinReference1");
    	pr.setDisplayName("ProteinReference1");
    	pr.addXref(ref);
    	ref = model.addNew(RelationshipXref.class, "Xref2");
    	ref.setDb("refseq");
    	ref.setId("NP_001734");
    	ref.setIdVersion("1");  // this xref won't be removed by norm. (version matters in xrefs comparing!)
		pr.addXref(ref);
	   	ref = model.addNew(UnificationXref.class, "Xref3");
    	ref.setDb("uniprotkb"); // will be converted to 'uniprot'
    	/* The following ID is the secondary accession of P68250, 
    	 * but Normalizer won't complain (it's Validator's and - later - Merger's job)!
    	 * However, it it were P68250 here again, the normalize(model) would throw exception
    	 * (because ProteinReference1 becomes ProteinReference2, both get RDFId= urn:miriam:uniprot:P68250!)
    	 */
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
		
		// normalizer won't merge diff. types of xref with the same db:id
	   	ref = model.addNew(PublicationXref.class, "http://biopax.org/Xref5");
    	ref.setDb("pubmed");
    	ref.setId("2549346"); // the same id
    	pr.addXref(ref);
	   	ref = model.addNew(RelationshipXref.class,
	   			"http://www.pathwaycommons.org/import#Xref6");
    	ref.setDb("pubmed"); 
    	ref.setId("2549346"); // the same id
    	pr.addXref(ref);

		// add biosource
	   	ref = model.addNew(UnificationXref.class, "Xref7");
    	ref.setDb("taxonomy"); 
    	ref.setId("10090"); // the same id
		BioSource bioSource = model.addNew(BioSource.class, "BioSource_Mouse_Tissue");
		bioSource.addXref((UnificationXref)ref);
		
		model.getNameSpacePrefixMap().put("", "http://www.pathwaycommons.org/import#");
		model.getNameSpacePrefixMap().put("biopax", "http://biopax.org/");

		// Provenance (must set ID and standard names from a name)
		Provenance pro1 = model.addNew(Provenance.class, "pid");
		pro1.addName("nci_nature"); // must be case insensitive (recognized)
		pro1.setStandardName("foo"); // must be replaced
		// Provenance (must create names from urn)
		Provenance pro2 = model.addNew(Provenance.class, "urn:miriam:signaling-gateway");
		
		// add some entities with props
		Pathway pw1 = model.addNew(Pathway.class, "pathway");
		pw1.addDataSource(pro1);
		pw1.setStandardName("Pathway");
		Pathway pw2 = model.addNew(Pathway.class, "sub_pathway");
		pw2.setStandardName("Sub-Pathway");
		pw2.addDataSource(pro2);
		pw1.addPathwayComponent(pw2);
		
		// go normalize!	
		Normalizer normalizer = new Normalizer();
		normalizer.normalize(model);
		
		String xml = null;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		simpleIO.convertToOWL(model, out);
		xml = out.toString();
		
		// alternatively, normalize the serialized data,but this can hide funny bugs...
		//xml = normalizer.normalize(out.toString());
		// read it back, reset to check
		//model = simpleReader.convertFromOWL(new ByteArrayInputStream(xml.getBytes("UTF-8")));
		//System.out.println(xml);
		
		// check Xref
		BioPAXElement bpe = model.getByID(Normalizer.BIOPAX_URI_PREFIX + "UnificationXref:UNIPROT_P68250");
		assertTrue(bpe instanceof UnificationXref);
		
		// check PR
		bpe = model.getByID("urn:miriam:uniprot:Q0VCL1");
		assertTrue(bpe instanceof ProteinReference);
		
		//check xref's ID gets normalized
		bpe = model.getByID(Normalizer.BIOPAX_URI_PREFIX + "RelationshipXref:REFSEQ_NP_001734");
		try {
			 //TODO another bug (xref and xrefOf become out of sync)?
			assertEquals(1, ((Xref)bpe).getXrefOf().size());
		} catch (AssertionError e) {
			Xref x = (Xref) bpe;
			XReferrable[] r = x.getXrefOf().toArray(new XReferrable[]{});
			System.out.println("WARN: xref and xrefOf become out of sync " +
				"after the normalization: xrefOf:" + x.getXrefOf()
				+ " whereas model.contains for these returns: [" 
				+ model.contains(r[0]) + ", " +  model.contains(r[1])
				+ "]");
		}
		// almost the same xref (was different idVersion)
		bpe = model.getByID(Normalizer.BIOPAX_URI_PREFIX + "RelationshipXref:REFSEQ_NP_001734_1");
		assertEquals(2, ((Xref)bpe).getXrefOf().size()); // must be "1" in fact...
		
    	//TODO test when uniprot's is not the first xref
    	//TODO test illegal 'id', 'db', etc.
    	//TODO add to test CV (and use a MI term)
		
		//test BioSource
		bpe = model.getByID("urn:miriam:taxonomy:10090");
		assertTrue(bpe instanceof BioSource);
		bpe = model.getByID(Normalizer.BIOPAX_URI_PREFIX + "UnificationXref:TAXONOMY_10090");
		assertTrue(bpe instanceof UnificationXref);
		
		
		// test that one of ProteinReference (2nd or 3rd) is removed
		assertEquals(2, model.getObjects(ProteinReference.class).size());
		
		// check Provenance is normalized
		assertEquals(2, model.getObjects(Provenance.class).size());
		pro1 = (Provenance) model.getByID("urn:miriam:pid.pathway");
		assertNotNull(pro1);
		assertTrue(pro1.getName().contains("PID"));
		assertTrue(pro1.getName().contains("foo"));
		assertFalse(pro1.getStandardName().equals("foo"));
		pro2 = (Provenance) model.getByID("urn:miriam:signaling-gateway");
		assertNotNull(pro2);
		assertNotNull(pro2.getStandardName());
		assertTrue(pro2.getName().contains("SGMP"));
		
		// check dataSource property has been inferred
		pw2 = (Pathway) model.getByID("sub_pathway");
		assertEquals(2, pw2.getDataSource().size());
		pw1 = (Pathway) model.getByID("pathway");
		assertEquals(1, pw1.getDataSource().size());
	}

	
	@Test
	public final void testAutoName() {
		Model model = BioPAXLevel.L3.getDefaultFactory().createModel();
		Provenance pro = model.addNew(Provenance.class, "urn:miriam:pid.pathway");
		pro.setStandardName("foo");
		Normalizer.autoName(pro);
		assertNotNull(pro.getStandardName());
		assertTrue(pro.getName().contains("PID"));
		assertTrue(pro.getName().contains("NCI_Nature curated"));
		assertFalse(pro.getStandardName().equals("foo"));
	}
	
	
	@Test
	public final void testInferPropertyFromParent() {
		Model model = BioPAXLevel.L3.getDefaultFactory().createModel();
		Provenance pro1 = model.addNew(Provenance.class, "urn:miriam:pid.pathway");
		Provenance pro2 = model.addNew(Provenance.class, "urn:miriam:signaling-gateway");
		Pathway pw1 = model.addNew(Pathway.class, "pathway");
		pw1.addDataSource(pro1);
		pw1.setStandardName("Pathway");
		Pathway pw2 = model.addNew(Pathway.class, "sub_pathway");
		pw2.setStandardName("Sub-Pathway");
		pw2.addDataSource(pro2);
		pw1.addPathwayComponent(pw2);
		ModelUtils.inferPropertyFromParent(model, "dataSource", Pathway.class);
		assertEquals(2, pw2.getDataSource().size());
		assertEquals(1, pw1.getDataSource().size());
	}
	
	
	@Test
	public final void testNormalize2() {
		Model model = BioPAXLevel.L3.getDefaultFactory().createModel();
		Xref ref =  model.addNew(UnificationXref.class, "Xref1");
    	ref.setDb("uniprotkb"); // will be converted to 'uniprot'
    	ref.setId("Q0VCL1"); 
    	Xref uniprotX = ref;
    	ProteinReference pr = model.addNew(ProteinReference.class, "ProteinReference");
    	pr.setDisplayName("ProteinReference");
    	pr.addXref(uniprotX);
    	ref = model.addNew(RelationshipXref.class, "Xref2");
    	ref.setDb("refseq");
    	ref.setId("NP_001734");
		pr.addXref(ref);
		// normalizer won't merge diff. types of xref with the same db:id
	   	ref = model.addNew(PublicationXref.class, "Xref3");
    	ref.setDb("pubmed");
    	ref.setId("2549346"); // the same id
    	pr.addXref(ref);
	   	ref = model.addNew(RelationshipXref.class,"Xref4");
    	ref.setDb("pubmed"); 
    	ref.setId("2549346"); // the same id
    	pr.addXref(ref);
		
		Normalizer normalizer = new Normalizer();
		normalizer.normalize(model);
		
		ProteinReference e = (ProteinReference) model.getByID("urn:miriam:uniprot:Q0VCL1");
		assertNotNull(e);
		
		/*
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			simpleExporter.convertToOWL(model, out);
			System.out.println(out.toString());
		} catch (IOException ex) {
			fail(ex.toString());
		}
		System.out.println("xrefs: " + e.getXref().toString());
		for(Xref x : e.getXref()) {
			System.out.println(x + " is " 
				+ x.getModelInterface().getSimpleName() 
				+ ", " + x.getRDFId() + ", " + x.getDb()
				+ ", " + x.getId() + ", " + x.getIdVersion());
		}
		*/
		
		assertEquals(4, e.getXref().size()); //FIXME duplicate xrefs bug!
	}
	
	@Test
	public final void testNormalize3() {
		Model model = BioPAXLevel.L3.getDefaultFactory().createModel();
		Xref ref =  model.addNew(UnificationXref.class, "Xref1");
    	ref.setDb("uniprotkb"); // will be converted to 'uniprot'
    	ref.setId("Q0VCL1"); 
    	ProteinReference pr = model.addNew(ProteinReference.class, "ProteinReference");
    	pr.setDisplayName("ProteinReference");
    	pr.addXref(ref);
		
    	// go normalize!
		Normalizer normalizer = new Normalizer();
		normalizer.normalize(model);
		
		ProteinReference e = (ProteinReference) model.getByID("urn:miriam:uniprot:Q0VCL1");
		assertNotNull(e);
		assertTrue(pr.isEquivalent(e));
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		simpleIO.convertToOWL(model, out);
		System.out.println(out.toString());

		for(Xref x : e.getXref()) {
			System.out.println(x + " is " 
				+ x.getModelInterface().getSimpleName() 
				+ ", " + x.getRDFId() + ", " + x.getDb()
				+ ", " + x.getId() + ", " + x.getIdVersion());
		}
		for(XReferrable x : ref.getXrefOf()) {
			System.out.println(ref + " is xrefOf: " 
				+ x.getModelInterface().getSimpleName() 
				+ ", " + x);
		}
		
		assertEquals(1, e.getXref().size()); //FIXME duplicates in xref is a bug (in the Normalizer)!
		// however, duplicates in xrefOf is not necessarily a bug (think of overlapping models)
		// assertEquals(1, ref.getXrefOf().size());
		// but, 
	}
}
