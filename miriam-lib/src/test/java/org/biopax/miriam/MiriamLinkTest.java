package org.biopax.miriam;

import static org.junit.Assert.*;

import java.util.*;

import net.biomodels.miriam.Resource;
import net.biomodels.miriam.Miriam.Datatype;

import org.biopax.miriam.MiriamLink;
import org.junit.Before;
import org.junit.Test;

public class MiriamLinkTest {
	
	static final String MIURN = "urn:miriam:obo.mi";
	static final String MIPAGE = "http://www.ebi.ac.uk/ontology-lookup/";
	static final String MIETRY_PREFIX = "http://www.ebi.ac.uk/ontology-lookup/?termId=";
	static final String MI = "Molecular Interactions Ontology";
	static final String MISYN = "mi";
	static final String MIRESID = "MIR:00100142";
	static final String MIID = "MIR:00000109";
	

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public final void testGetServicesVersion() {
		String version = MiriamLink.getServicesVersion();
		assertNotNull(version);
	}

	@Test
	public final void testGetDataTypeURI() {
		assertEquals(MIURN, MiriamLink.getDataTypeURI(MI));
		assertEquals(MIURN, MiriamLink.getDataTypeURI(MISYN));
		assertEquals(MIURN, MiriamLink.getDataTypeURI(MIURN));
	}

	@Test
	public final void testGetDataTypeURIs() {
		String[] uris = MiriamLink.getDataTypeURIs(MI);
		assertTrue(uris.length==1);
		assertEquals(MIURN, uris[0]);
	}

	@Test
	public final void testGetResourceLocation() {
		assertEquals("UK", MiriamLink.getResourceLocation(MIRESID));
	}

	@Test
	public final void testGetResourceInstitution() {
		assertEquals("European Bioinformatics Institute", MiriamLink.getResourceInstitution(MIRESID));
	}

	@Test
	public final void testGetURI() {
		assertEquals("urn:miriam:obo.mi:MI%3A0000", MiriamLink.getURI(MISYN, "MI:0000"));
		
		try{
			MiriamLink.getURI(MISYN, "MI_0000");
			fail("must throw IllegalArgumentException (wrong ID format)");
		} catch (IllegalArgumentException e) {
		}
	}

	@Test
	public final void testGetDataTypeDef() {
		String def = MiriamLink.getDataTypeDef(MISYN);
		assertNotNull(def);
		assertTrue(def.contains("MI is developed by"));
	}

	@Test
	public final void testGetLocations() {
		String[] locs = MiriamLink.getLocations(MI, "MI:0000");
		assertTrue(locs.length>0);
		assertEquals("http://www.ebi.ac.uk/ontology-lookup/?termId=MI%3A0000", locs[0]);
	}

	@Test
	public final void testGetDataResources() {
		String[] drs = MiriamLink.getDataResources(MI);
		assertTrue(drs.length == 1);
		assertEquals("http://www.ebi.ac.uk/ontology-lookup/", drs[0]);
	}

	@Test
	public final void testIsDeprecated() {
		assertFalse(MiriamLink.isDeprecated("urn:miriam:hmdb"));
		assertTrue(MiriamLink.isDeprecated("http://www.hmdb.ca/"));
	}

	@Test
	public final void testGetDataTypePattern() {
		assertEquals("^MI:\\d{4}$", MiriamLink.getDataTypePattern(MI));
	}


	@Test
	public final void testGetName() {
		assertEquals(MI, MiriamLink.getName(MIURN));
	}

	
	public final void testGetNames() {
		String[] names = MiriamLink.getNames(MIURN);
		assertTrue(names.length==2);
		String s = names[0] + names[1];
		assertTrue(s.contains("MI"));
		assertTrue(s.contains(MI));
	}

	@Test
	public final void testGetDataTypesName() {
		String[] dts = MiriamLink.getDataTypesName();
		List<String> names = Arrays.asList(dts);
		assertFalse(names.contains("MI"));
		assertTrue(names.contains(MI));
		assertTrue(names.contains("CluSTr"));
	}

	@Test
	public final void testGetDataTypesId() {
		String[] dts = MiriamLink.getDataTypesId();
		List<String> names = Arrays.asList(dts);
		assertTrue(names.contains(MIID));
		assertTrue(names.contains("MIR:00000021"));
	}

	@Test
	public final void testCheckRegExp() {
		assertTrue(MiriamLink.checkRegExp("MI:0000", MI));
		assertFalse(MiriamLink.checkRegExp("0000", MI));
	}

	@Test
	public final void testGetOfficialDataTypeURIDatatype() {
		Datatype dt = MiriamLink.getDatatype(MI);
		assertNotNull(dt);
		assertEquals(MIID, dt.getId());
		String urn = MiriamLink.getOfficialDataTypeURI(dt);
		assertEquals(MIURN, urn);
	}

	@Test
	public final void testGetResourcesId() {
		String[] rs = MiriamLink.getResourcesId();
		List<String> names = Arrays.asList(rs);
		assertTrue(names.contains(MIRESID));
		assertTrue(names.contains("MIR:00100096"));
	}

	@Test
	public final void testGetResource() {
		Resource resource = MiriamLink.getResource("MIR:00100008");
		assertNotNull(resource);
		assertEquals("Canada", resource.getDataLocation());
	}

}
