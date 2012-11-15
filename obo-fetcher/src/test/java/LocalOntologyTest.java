import static org.junit.Assert.*;

import org.biopax.psidev.ontology_manager.*;
import org.biopax.psidev.ontology_manager.impl.*;
import org.junit.*;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;


import java.util.*;

//@Ignore
public class LocalOntologyTest {

	static OntologyManager manager;
	static Ontology mod;
	static Ontology mi;
	static Ontology so;

	static {
		OntologyManagerContext.getInstance().setStoreOntologiesLocally(true);
		final Properties cfg = new Properties();
		cfg.put("SO", "http://song.cvs.sourceforge.net/viewvc/song/ontology/so.obo?revision=1.310");
		cfg.put("MI", "http://psidev.cvs.sourceforge.net/viewvc/psidev/psi/mi/rel25/data/psi-mi25.obo?revision=1.58");
		cfg.put("MOD", "http://psidev.cvs.sourceforge.net/viewvc/psidev/psi/mod/data/PSI-MOD.obo?revision=1.23");
		
		try {
			manager = new OntologyManagerImpl(cfg);
			mod = manager.getOntology("MOD");
			mi = manager.getOntology("MI");
			so = manager.getOntology("SO");
		} catch (OntologyLoaderException e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void getValidTerms() throws OntologyLoaderException {
		final Set<OntologyTermI> terms = mod.getValidTerms("MOD:00647", true,
				false);
		assertEquals(3, terms.size());
	}

	@Test
	public void getMiTermSynonyms() throws OntologyLoaderException {
		// GO:0055044 has 7 children (OLS 17 July 2008) = 7 valid terms
		final Set<OntologyTermI> terms = mi.getValidTerms("MI:0018", false,
				true);
		assertEquals(1, terms.size());
		final OntologyTermI y2h = terms.iterator().next();

		assertEquals(8, y2h.getNameSynonyms().size());
		assertTrue(y2h.getNameSynonyms().contains("2h"));
		assertTrue(y2h.getNameSynonyms()
				.contains("classical two hybrid"));
		assertTrue(y2h.getNameSynonyms().contains(
				"Gal4 transcription regeneration"));
		assertTrue(y2h.getNameSynonyms().contains("2 hybrid"));
		assertTrue(y2h.getNameSynonyms().contains("two-hybrid"));
		assertTrue(y2h.getNameSynonyms().contains("2H"));
		assertTrue(y2h.getNameSynonyms().contains("yeast two hybrid"));
		assertTrue(y2h.getNameSynonyms().contains("2-hybrid"));
	}

	// there was a problem with this particular term!
	@Test
	public void getMiTermSynonyms0217() throws OntologyLoaderException {
		final Set<OntologyTermI> terms = mi.getValidTerms("MI:0217", false,
				true);
		assertEquals(1, terms.size());

		final OntologyTermI phosphorylation = mi.getTermForAccession("MI:0217");
		assertEquals(1, phosphorylation.getNameSynonyms().size());

		// different approach
		Collection<String> names;
		names = OntologyUtils.getTermNames(terms);
		assertTrue(names.contains("phosphorylation"));
		assertTrue(names.contains("phosphorylation reaction"));
	}

	@Test
	public void getModTermSynonyms() throws OntologyLoaderException {
		final Set<OntologyTermI> terms = mod.getValidTerms("MOD:00007", false,
				true);
		assertEquals(1, terms.size());
		final OntologyTermI term = terms.iterator().next();

		assertEquals(3, term.getNameSynonyms().size());
		assertTrue(term.getNameSynonyms().contains("Delta:S(-1)Se(1)"));
		assertTrue(term.getNameSynonyms().contains("Se(S)Res"));
		assertTrue(term.getNameSynonyms().contains(
				"Selenium replaces sulphur"));
	}

	@Test
	public void isObsolete() throws Exception {
		final OntologyTermI term = mi.getTermForAccession("MI:0205");
		assertTrue(mi.isObsolete(term));

		final OntologyTermI term2 = mi.getTermForAccession("MI:0001");
		assertFalse(mi.isObsolete(term2));
	}

	@Test
	public void isObsolete_unknown_accession() throws Exception {
		final OntologyTermI term = new OntologyTermImpl("MI", "MI:xxxx",
				"bogus term");
		assertFalse(mi.isObsolete(term));
	}

	@Test
	public void getTermForAccession() throws Exception {
		final OntologyTermI term = mi.getTermForAccession("MI:0013");
		assertNotNull(term);
		assertEquals("MI:0013", term.getTermAccession());
		assertEquals("biophysical", term.getPreferredName());
	}

	@Test
	public void getTermForAccession_unknown_accession() throws Exception {
		final OntologyTermI term = mi.getTermForAccession("MI:xxxx");
		assertNull(term);
	}

	// ////////////////
	// Children

	@Test
	public void getDirectChildren() throws Exception {
		final OntologyTermI term = mi.getTermForAccession("MI:0417"); // footprinting
		assertNotNull(term);

		final Set<OntologyTermI> children = mi.getDirectChildren(term);
		assertNotNull(children);
		assertEquals(2, children.size());
		assertTrue(children.contains(new OntologyTermImpl("MI",
				"MI:0602", "chemical footprinting")));
		assertTrue(children.contains(new OntologyTermImpl("MI",
				"MI:0605", "enzymatic footprinting")));
	}

	@Test
	public void getDirectChildren_unknown_accession() throws Exception {
		final OntologyTermI term = new OntologyTermImpl("MI", "MI:xxxx",
				"bogus term");

		final Set<OntologyTermI> children = mi.getDirectChildren(term);
		assertNotNull(children);
		assertEquals(0, children.size());
	}

	@Test
	public void getAllChildren() throws Exception {
		final OntologyTermI term = mi.getTermForAccession("MI:0417"); // footprinting
		assertNotNull(term);

		final Set<OntologyTermI> children = mi.getAllChildren(term);
		assertNotNull(children);
		assertEquals(children.toString(), 7, children.size());
		assertTrue(children.contains(new OntologyTermImpl("MI",
				"MI:0602", "chemical footprinting")));
		assertTrue(children.contains(new OntologyTermImpl("MI",
				"MI:0605", "enzymatic footprinting")));
		assertTrue(children.contains(new OntologyTermImpl("MI",
				"MI:0603", "dimethylsulphate footprinting")));
		assertTrue(children.contains(new OntologyTermImpl("MI",
				"MI:0604", "potassium permanganate footprinting")));
		assertTrue(children.contains(new OntologyTermImpl("MI",
				"MI:0606", "DNase I footprinting")));
		assertTrue(children.contains(new OntologyTermImpl("MI",
				"MI:0814", "protease accessibility laddering")));
		assertTrue(children.contains(new OntologyTermImpl("MI",
				"MI:0901", "isotope label footprinting")));
	}

	@Test
	public void getAllChildren_unknown_accession() throws Exception {
		final OntologyTermI term = new OntologyTermImpl("MI", "MI:xxxx",
				"bogus term");

		final Set<OntologyTermI> children = mi.getAllChildren(term);
		assertNotNull(children);
		assertEquals(0, children.size());
	}

	// /////////////////

	// Parents

	@Test
	public void getDirectParents() throws Exception {
		final OntologyTermI term = mi.getTermForAccession("MI:0013");
		assertNotNull(term);

		final Set<OntologyTermI> parents = mi.getDirectParents(term);
		assertNotNull(parents);
		assertEquals(1, parents.size());
		assertTrue(parents.contains(new OntologyTermImpl("MI",
				"MI:0045", "experimental interaction detection")));
	}

	@Test
	public void getDirectParents_unknown_accession() throws Exception {
		final OntologyTermImpl term = new OntologyTermImpl("MI", "MI:xxxx",
				"bogus term");
		final Set<OntologyTermI> parents = mi.getDirectParents(term);
		assertNotNull(parents);
		assertEquals(0, parents.size());
	}

	@Test
	public void getAllParents() throws Exception {
		final OntologyTermI term = mi.getTermForAccession("MI:0013");
		assertNotNull(term);

		final Set<OntologyTermI> parents = mi.getAllParents(term);
		assertNotNull(parents);
		assertEquals(3, parents.size());
		assertTrue(parents.contains(new OntologyTermImpl("MI",
				"MI:0045", "experimental interaction detection")));
		assertTrue(parents.contains(new OntologyTermImpl("MI",
				"MI:0001", "interaction detection method")));
		assertTrue(parents.contains(new OntologyTermImpl("MI",
				"MI:0000", "molecular interaction")));
	}

	@Test
	public void getAllParents_unknown_accession() throws Exception {
		final OntologyTermImpl term = new OntologyTermImpl("MI", "MI:xxxx",
				"bogus term");

		final Set<OntologyTermI> parents = mi.getAllParents(term);
		assertNotNull(parents);
		assertEquals(0, parents.size());
	}

	private void printTerms(Collection<OntologyTermI> terms) {
		for (OntologyTermI term : terms) {
			System.out.println(term);
		}
	}

	@Test
	public void getValidTerms_so_small() throws OntologyLoaderException {
		// GO:0055044 has 7 children (OLS 17 July 2008) = 7 valid terms
		OntologyTermI parent = so.getTermForAccession("SO:0000805");
		Set<OntologyTermI> terms = so.getAllChildren(parent);
		assertEquals(4, terms.size());
	}

	@Test
	public void getValidTerms_so_large() throws OntologyLoaderException {
		OntologyTermI parent = so.getTermForAccession("SO:0000001");
		Set<OntologyTermI> terms = so.getAllChildren(parent);
		assertTrue(terms.size() > 10);
	}
	
	
	@Test
	public final void testSearchTermByName() {
		Set<OntologyTermI> term = manager.searchTermByName("O-phospho-L-serine");
		assertFalse(term.isEmpty());
	}


	@Test
	public final void testTermByAccession() {
		OntologyTermI term = mod.getTermForAccession("MOD:00046");
		assertNotNull(term);
		assertEquals("MOD", term.getOntologyId());
		// so far so good...
		term = manager.findTermByAccession("MOD:00046");
		assertNotNull(term);
		assertEquals("MOD", term.getOntologyId());
	}
	
	@Test
	public void getModChildren01157() throws OntologyLoaderException {
		Set<OntologyTermI> terms = mod.getValidTerms("MOD:01157", true, false);
		assertFalse(terms.isEmpty());
		assertTrue(OntologyUtils.getAccessions(terms).contains("MOD:00036"));
		assertTrue(OntologyUtils.getTermNames(terms).contains("(2S,3R)-3-hydroxyaspartic acid"));
	}
}
