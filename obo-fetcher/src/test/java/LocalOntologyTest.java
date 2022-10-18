import static org.junit.Assert.*;

import org.biopax.psidev.ontology_manager.*;
import org.biopax.psidev.ontology_manager.impl.*;
import org.junit.*;

import java.util.*;

@Ignore
public class LocalOntologyTest {

	static OntologyManager manager;
	static OntologyAccess mod;
	static OntologyAccess mi;

	static {
		final Properties cfg = new Properties();
		cfg.put("MI", "classpath:test-mi.obo");
		cfg.put("MOD", "classpath:test-mod.obo");
		try {
			manager = new OntologyManagerImpl();
			manager.loadOntologies(cfg);
			mod = manager.getOntology("MOD");
			mi = manager.getOntology("MI");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void getValidTerms() {
		final Set<OntologyTermI> terms = mod.getValidTerms("MOD:00647", true,
				false);
		assertEquals(3, terms.size());
	}

	@Test
	public void getMiTermSynonyms() {
		// GO:0055044 has 7 children (OLS 17 July 2008) = 7 valid terms
		final Set<OntologyTermI> terms = mi.getValidTerms("MI:0018", false,
				true);
		assertEquals(1, terms.size());
		final OntologyTermI y2h = terms.iterator().next();

		assertEquals(10, y2h.getNameSynonyms().size());
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
	public void getMiTermSynonyms0217() {
		final Set<OntologyTermI> terms = mi.getValidTerms("MI:0217", false,
				true);
		assertEquals(1, terms.size());

		final OntologyTermI phosphorylation = mi.getTermForAccession("MI:0217");
		assertEquals(1, phosphorylation.getNameSynonyms().size());

		// different approach
		Collection<String> names;
		names = getTermNames(terms);
		assertTrue(names.contains("phosphorylation"));
		assertTrue(names.contains("phosphorylation reaction"));
	}

	@Test
	public void getModTermSynonyms() {
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
	public void isObsolete() {
		final OntologyTermI term = mi.getTermForAccession("MI:0205");
		assertTrue(mi.isObsolete(term));

		final OntologyTermI term2 = mi.getTermForAccession("MI:0001");
		assertFalse(mi.isObsolete(term2));
	}

	@Test
	public void isObsolete_unknown_accession() {
		final OntologyTermI term = new OntologyTermImpl("MI", "MI:xxxx",
				"bogus term");
		assertFalse(mi.isObsolete(term));
	}

	@Test
	public void getTermForAccession() {
		final OntologyTermI term = mi.getTermForAccession("MI:0013");
		assertNotNull(term);
		assertEquals("MI:0013", term.getTermAccession());
		assertEquals("biophysical", term.getPreferredName());
	}

	@Test
	public void getTermForAccession_unknown_accession()  {
		final OntologyTermI term = mi.getTermForAccession("MI:xxxx");
		assertNull(term);
	}

	// Children

	@Test
	public void getDirectChildren() {
		final OntologyTermI term = mi.getTermForAccession("MI:0417"); // footprinting
		assertNotNull(term);

		final Set<OntologyTermI> children = mi.getDirectChildren(term);
		assertNotNull(children);
		assertEquals(3, children.size());
		assertTrue(children.contains(new OntologyTermImpl("MI",
				"MI:0602", "chemical footprinting")));
		assertTrue(children.contains(new OntologyTermImpl("MI",
				"MI:0605", "enzymatic footprinting")));
	}

	@Test
	public void getDirectChildren_unknown_accession() {
		final OntologyTermI term = new OntologyTermImpl("MI", "MI:xxxx",
				"bogus term");

		final Set<OntologyTermI> children = mi.getDirectChildren(term);
		assertNotNull(children);
		assertEquals(0, children.size());
	}

	@Test
	public void getAllChildren() {
		final OntologyTermI term = mi.getTermForAccession("MI:0417"); // footprinting
		assertNotNull(term);

		final Set<OntologyTermI> children = mi.getAllChildren(term);
		assertNotNull(children);
		assertEquals(children.toString(), 12, children.size());
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
	public void getAllChildren_unknown_accession() {
		final OntologyTermI term = new OntologyTermImpl("MI", "MI:xxxx",
				"bogus term");

		final Set<OntologyTermI> children = mi.getAllChildren(term);
		assertNotNull(children);
		assertEquals(0, children.size());
	}

	// Parents

	@Test
	public void getDirectParents() {
		final OntologyTermI term = mi.getTermForAccession("MI:0013");
		assertNotNull(term);

		final Set<OntologyTermI> parents = mi.getDirectParents(term);
		assertNotNull(parents);
		assertEquals(1, parents.size());
		assertTrue(parents.contains(new OntologyTermImpl("MI",
				"MI:0045", "experimental interaction detection")));
	}

	@Test
	public void getDirectParents_unknown_accession() {
		final OntologyTermImpl term = new OntologyTermImpl("MI", "MI:xxxx",
				"bogus term");
		final Set<OntologyTermI> parents = mi.getDirectParents(term);
		assertNotNull(parents);
		assertEquals(0, parents.size());
	}

	@Test
	public void getAllParents() {
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
	public void getAllParents_unknown_accession() {
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
	public void getModChildren01157() {
		Set<OntologyTermI> terms = mod.getValidTerms("MOD:01157", true, false);
		assertFalse(terms.isEmpty());
		assertTrue(getAccessions(terms).contains("MOD:00036"));
		assertTrue(getTermNames(terms).contains("(2S,3R)-3-hydroxyaspartic acid"));
	}
	

    static Collection<String> getAccessions(  Collection<OntologyTermI> terms ) {
        Collection<String> accessions = new ArrayList<String>( terms.size() );
        for ( OntologyTermI term : terms ) {
            accessions.add( term.getTermAccession() );
        }
        return accessions;
    }
    
    static Collection<String> getTermNames(  Collection<OntologyTermI> terms ) {
        Collection<String> names = new ArrayList<String>( terms.size() );
        for ( OntologyTermI term : terms ) {
            names.add( term.getPreferredName() );
            names.addAll( term.getNameSynonyms() );
        }
        return names;
    }
}
