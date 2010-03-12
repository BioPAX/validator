package org.biopax.miriam;

import static org.junit.Assert.*;

import org.biopax.miriam.MiriamLink;
import org.junit.Before;
import org.junit.Test;

public class MiriamLinkTest {
	
	static MiriamLink link = new MiriamLink();
	static final String MIURN = "urn:miriam:obo.mi";
	static final String MIPAGE = "http://www.ebi.ac.uk/ontology-lookup/";
	static final String MIETRY_PREFIX = "http://www.ebi.ac.uk/ontology-lookup/?termId=";
	static final String MI = "Molecular Interactions Ontology";
	static final String MISYN = "mi";

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public final void testGetServicesVersion() {
		String version = link.getServicesVersion();
		assertNotNull(version);
	}

	@Test
	public final void testGetDataTypeURI() {
		assertEquals(MIURN, link.getDataTypeURI(MI));
		assertEquals(MIURN, link.getDataTypeURI(MISYN));
		assertEquals(MIURN, link.getDataTypeURI(MIURN));
	}

	@Test
	public final void testGetDataTypeURIs() {
		String[] uris = link.getDataTypeURIs(MI);
		assertTrue(uris.length==1);
		assertEquals(MIURN, uris[0]);
	}

	//@Test
	public final void testGetResourceLocation() {
		fail("Not yet implemented"); // TODO
	}

	//@Test
	public final void testGetResourceInstitution() {
		fail("Not yet implemented"); // TODO
	}

	//@Test
	public final void testGetURI() {
		fail("Not yet implemented"); // TODO
	}

	//@Test
	public final void testGetDataTypeDef() {
		fail("Not yet implemented"); // TODO
	}

	//@Test
	public final void testGetLocations() {
		fail("Not yet implemented"); // TODO
	}

	//@Test
	public final void testGetDataResources() {
		fail("Not yet implemented"); // TODO
	}

	//@Test
	public final void testIsDeprecated() {
		fail("Not yet implemented"); // TODO
	}

	//@Test
	public final void testGetDataTypePattern() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void testGetDataTypeSynonyms() {
		String[] synons = link.getDataTypeSynonyms(MI);
		assertTrue(synons.length==1);
		assertEquals(MISYN.toLowerCase(), synons[0].toLowerCase());
	}

	//@Test
	public final void testGetName() {
		fail("Not yet implemented"); // TODO
	}

	//@Test
	public final void testGetNames() {
		fail("Not yet implemented"); // TODO
	}

	//@Test
	public final void testGetDataTypesName() {
		fail("Not yet implemented"); // TODO
	}

	//@Test
	public final void testGetDataTypesId() {
		fail("Not yet implemented"); // TODO
	}

	//@Test
	public final void testCheckRegExp() {
		fail("Not yet implemented"); // TODO
	}

	//@Test
	public final void testGetOfficialDataTypeURIDatatype() {
		fail("Not yet implemented"); // TODO
	}

	//@Test
	public final void testGetDatatype() {
		fail("Not yet implemented"); // TODO
	}

	//@Test
	public final void testGetResourcesId() {
		fail("Not yet implemented"); // TODO
	}

	//@Test
	public final void testGetResource() {
		fail("Not yet implemented"); // TODO
	}

}
