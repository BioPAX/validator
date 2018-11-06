package org.biopax.validator.utils;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.Properties;
import java.util.Set;

import org.biopax.paxtools.model.level3.*;
import org.biopax.validator.CvFactory;
import org.biopax.validator.XrefUtils;
import org.biopax.validator.api.CvUtils;
import org.junit.BeforeClass;
import org.junit.Test;


//test utils interfaces:  CvUtils, CvFactory, XrefUtils
public class CvRepositoryTest {

	static CvUtils cvUtils;
	static CvFactory cvFactory;
  static XrefUtils xrefUtils;

	@BeforeClass
	public static void init() {
		final Properties cfg = new Properties();
		cfg.put("GO", "classpath:test-go.obo");
		cfg.put("MOD", "classpath:test-mod.obo");
    OntologyUtils utils = new OntologyUtils();
    utils.init();
    cvUtils = utils;
    cvFactory = utils;
    xrefUtils = utils;
	}

	@Test
  public void xcheck() {
	  assertTrue(xrefUtils.xcheck());
  }

	@Test
	public void ontologyLoading() {
		Collection<String> ontologyIDs = cvUtils.getOntologyManager().getOntologyIDs();
		assertTrue(ontologyIDs.contains("GO"));
		assertEquals("gene ontology", cvUtils
      .getOntologyManager().getOntology("GO").getName().toLowerCase());
		assertTrue(ontologyIDs.contains("MOD"));
	}

	@Test
	public void testGetDirectChildren() {
		Set<String> dc = cvFactory.getDirectChildren("urn:miriam:obo.go:GO%3A0005654");
		assertFalse(dc.isEmpty());
		assertTrue(dc.contains("http://identifiers.org/go/GO:0044451"));
	}

	@Test
	public void testGetAllChildren() {
		Set<String> dc = cvFactory.getAllChildren("http://identifiers.org/obo.go/GO:0005654");
		assertFalse(dc.isEmpty());
		assertTrue(dc.contains("http://identifiers.org/go/GO:0044451"));
		assertTrue(dc.contains("http://identifiers.org/go/GO:0071821"));
		assertTrue(dc.contains("http://identifiers.org/go/GO:0070847"));
	}

	@Test
	public void testGetDirectParents() {
		Set<String> dc = cvFactory.getDirectParents("urn:miriam:obo.go:GO%3A0005654");
		assertFalse(dc.isEmpty());
		assertTrue(dc.contains("http://identifiers.org/go/GO:0031981"));
	}

	@Test
	public void testGetAllParents() {
		Set<String> dc = cvFactory.getAllParents("http://identifiers.org/obo.go/GO:0005654");
		assertFalse(dc.isEmpty());
		assertTrue(dc.contains("http://identifiers.org/go/GO:0031981"));
		assertTrue(dc.contains("http://identifiers.org/go/GO:0044428"));
		assertTrue(dc.contains("http://identifiers.org/go/GO:0044422"));
	}

	@Test // using correct ID(s)
	public void testGetObject() {
		CellularLocationVocabulary cv = cvFactory.getControlledVocabulary("urn:miriam:obo.go:GO%3A0005737", CellularLocationVocabulary.class,"");
		assertNotNull(cv);
		cv = null;
		cv = cvFactory.getControlledVocabulary( //using now deprecated URL
				"http://identifiers.org/obo.go/GO:0005737",CellularLocationVocabulary.class,"");
		assertNotNull(cv);
		//same
		cv = cvFactory.getControlledVocabulary(
				"http://identifiers.org/go/GO:0005737",CellularLocationVocabulary.class,"");
		assertNotNull(cv);
	}
	
	@Test // using bad ID (with 'X' in it)
	public void testGetObject2() {
		CellularLocationVocabulary cv = cvFactory.getControlledVocabulary(
				"urn:miriam:obo.go:GO%3A0005737X",CellularLocationVocabulary.class,"");
		assertNull(cv);
	}

	@Test 
	public void testEscapeChars() {
		ControlledVocabulary cv = cvFactory.getControlledVocabulary(
				"http://identifiers.org/obo.psi-mod/MOD:00048",SequenceModificationVocabulary.class,"");
		assertNotNull(cv);
		assertTrue(cv.getTerm().contains("O4'-phospho-L-tyrosine")); // apostrophe
	}
	
}
