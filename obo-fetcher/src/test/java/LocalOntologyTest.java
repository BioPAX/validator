import static org.junit.Assert.*;
import org.junit.*;

import psidev.ontology_manager.Ontology;
import psidev.ontology_manager.OntologyManager;
import psidev.ontology_manager.OntologyTermI;
import psidev.ontology_manager.impl.OntologyImpl;
import psidev.ontology_manager.impl.OntologyLoaderException;
import psidev.ontology_manager.impl.OntologyManagerContext;
import psidev.ontology_manager.impl.OntologyManagerImpl;
import psidev.ontology_manager.impl.OntologyTermImpl;
import psidev.ontology_manager.impl.OntologyUtils;

import java.io.InputStream;
import java.util.Collection;
import java.util.Set;

/**
 * OntologyImpl Tester.
 * 
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @author rodche (baderlab.org) - refactoring
 * @version $Id$
 * @since 2.0.0
 */

public class LocalOntologyTest {

	static OntologyManager manager;
	Ontology mod;
	Ontology mi;
	Ontology so;

	static {
		OntologyManagerContext.getInstance().setStoreOntologiesLocally(true);
		final InputStream config = LocalOntologyTest.class
				.getResourceAsStream("/ontologies.xml");
		try {
			manager = new OntologyManagerImpl(config);
		} catch (OntologyLoaderException e) {
			fail(e.toString());
		}
	}

	public LocalOntologyTest() {
		mod = manager.getOntology("MOD");
		mi = manager.getOntology("MI");
		so = manager.getOntology("SO");
	}

	@Test
	public void ontologyLoading() {
		Collection<String> ontologyIDs = manager.getOntologyIDs();
		Assert.assertTrue(ontologyIDs.contains("MOD"));
		Assert.assertTrue(ontologyIDs.contains("SO"));
		Assert.assertTrue(ontologyIDs.contains("MI"));

		Ontology oa2 = manager.getOntology("MOD");
		Assert.assertNotNull(oa2);
		Assert.assertTrue(oa2 instanceof OntologyImpl);

		oa2 = manager.getOntology("SO");
		Assert.assertNotNull(oa2);
		Assert.assertTrue(oa2 instanceof OntologyImpl);

		oa2 = manager.getOntology("MI");
		Assert.assertNotNull(oa2);
		Assert.assertTrue(oa2 instanceof OntologyImpl);
	}

	@Test
	public void getValidTerms() throws OntologyLoaderException {
		final Set<OntologyTermI> terms = mod.getValidTerms("MOD:00647", true,
				false);
		Assert.assertEquals(3, terms.size());
	}

	@Test
	public void getMiTermSynonyms() throws OntologyLoaderException {
		// GO:0055044 has 7 children (OLS 17 July 2008) = 7 valid terms
		final Set<OntologyTermI> terms = mi.getValidTerms("MI:0018", false,
				true);
		Assert.assertEquals(1, terms.size());
		final OntologyTermI y2h = terms.iterator().next();

		Assert.assertEquals(8, y2h.getNameSynonyms().size());
		Assert.assertTrue(y2h.getNameSynonyms().contains("2h"));
		Assert.assertTrue(y2h.getNameSynonyms()
				.contains("classical two hybrid"));
		Assert.assertTrue(y2h.getNameSynonyms().contains(
				"Gal4 transcription regeneration"));
		Assert.assertTrue(y2h.getNameSynonyms().contains("2 hybrid"));
		Assert.assertTrue(y2h.getNameSynonyms().contains("two-hybrid"));
		Assert.assertTrue(y2h.getNameSynonyms().contains("2H"));
		Assert.assertTrue(y2h.getNameSynonyms().contains("yeast two hybrid"));
		Assert.assertTrue(y2h.getNameSynonyms().contains("2-hybrid"));
	}

	// there was a problem with this particular term!
	@Test
	public void getMiTermSynonyms0217() throws OntologyLoaderException {
		final Set<OntologyTermI> terms = mi.getValidTerms("MI:0217", false,
				true);
		Assert.assertEquals(1, terms.size());

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
		// GO:0055044 has 7 children (OLS 17 July 2008) = 7 valid terms
		final Set<OntologyTermI> terms = mod.getValidTerms("MOD:00007", false,
				true);
		Assert.assertEquals(1, terms.size());
		final OntologyTermI term = terms.iterator().next();

		Assert.assertEquals(3, term.getNameSynonyms().size());
		Assert.assertTrue(term.getNameSynonyms().contains("Delta:S(-1)Se(1)"));
		Assert.assertTrue(term.getNameSynonyms().contains("Se(S)Res"));
		Assert.assertTrue(term.getNameSynonyms().contains(
				"Selenium replaces sulphur"));
	}

	@Test
	public void isObsolete() throws Exception {
		final OntologyTermI term = mi.getTermForAccession("MI:0205");
		Assert.assertTrue(mi.isObsolete(term));

		final OntologyTermI term2 = mi.getTermForAccession("MI:0001");
		Assert.assertFalse(mi.isObsolete(term2));
	}

	@Test
	public void isObsolete_unknown_accession() throws Exception {
		final OntologyTermI term = new OntologyTermImpl("MI", "MI:xxxx",
				"bogus term");
		Assert.assertFalse(mi.isObsolete(term));
	}

	@Test
	public void getTermForAccession() throws Exception {
		final OntologyTermI term = mi.getTermForAccession("MI:0013");
		Assert.assertNotNull(term);
		Assert.assertEquals("MI:0013", term.getTermAccession());
		Assert.assertEquals("biophysical", term.getPreferredName());
	}

	@Test
	public void getTermForAccession_unknown_accession() throws Exception {
		final OntologyTermI term = mi.getTermForAccession("MI:xxxx");
		Assert.assertNull(term);
	}

	// ////////////////
	// Children

	@Test
	public void getDirectChildren() throws Exception {
		final OntologyTermI term = mi.getTermForAccession("MI:0417"); // footprinting
		Assert.assertNotNull(term);

		final Set<OntologyTermI> children = mi.getDirectChildren(term);
		Assert.assertNotNull(children);
		Assert.assertEquals(2, children.size());
		Assert.assertTrue(children.contains(new OntologyTermImpl("MI",
				"MI:0602", "chemical footprinting")));
		Assert.assertTrue(children.contains(new OntologyTermImpl("MI",
				"MI:0605", "enzymatic footprinting")));
	}

	@Test
	public void getDirectChildren_unknown_accession() throws Exception {
		final OntologyTermI term = new OntologyTermImpl("MI", "MI:xxxx",
				"bogus term");

		final Set<OntologyTermI> children = mi.getDirectChildren(term);
		Assert.assertNotNull(children);
		Assert.assertEquals(0, children.size());
	}

	@Test
	public void getAllChildren() throws Exception {
		final OntologyTermI term = mi.getTermForAccession("MI:0417"); // footprinting
		Assert.assertNotNull(term);

		final Set<OntologyTermI> children = mi.getAllChildren(term);
		Assert.assertNotNull(children);
		Assert.assertEquals(children.toString(), 7, children.size());
		Assert.assertTrue(children.contains(new OntologyTermImpl("MI",
				"MI:0602", "chemical footprinting")));
		Assert.assertTrue(children.contains(new OntologyTermImpl("MI",
				"MI:0605", "enzymatic footprinting")));
		Assert.assertTrue(children.contains(new OntologyTermImpl("MI",
				"MI:0603", "dimethylsulphate footprinting")));
		Assert.assertTrue(children.contains(new OntologyTermImpl("MI",
				"MI:0604", "potassium permanganate footprinting")));
		Assert.assertTrue(children.contains(new OntologyTermImpl("MI",
				"MI:0606", "DNase I footprinting")));
		Assert.assertTrue(children.contains(new OntologyTermImpl("MI",
				"MI:0814", "protease accessibility laddering")));
		Assert.assertTrue(children.contains(new OntologyTermImpl("MI",
				"MI:0901", "isotope label footprinting")));
	}

	@Test
	public void getAllChildren_unknown_accession() throws Exception {
		final OntologyTermI term = new OntologyTermImpl("MI", "MI:xxxx",
				"bogus term");

		final Set<OntologyTermI> children = mi.getAllChildren(term);
		Assert.assertNotNull(children);
		Assert.assertEquals(0, children.size());
	}

	// /////////////////

	// Parents

	@Test
	public void getDirectParents() throws Exception {
		final OntologyTermI term = mi.getTermForAccession("MI:0013");
		Assert.assertNotNull(term);

		final Set<OntologyTermI> parents = mi.getDirectParents(term);
		Assert.assertNotNull(parents);
		Assert.assertEquals(1, parents.size());
		Assert.assertTrue(parents.contains(new OntologyTermImpl("MI",
				"MI:0045", "experimental interaction detection")));
	}

	@Test
	public void getDirectParents_unknown_accession() throws Exception {
		final OntologyTermImpl term = new OntologyTermImpl("MI", "MI:xxxx",
				"bogus term");
		final Set<OntologyTermI> parents = mi.getDirectParents(term);
		Assert.assertNotNull(parents);
		Assert.assertEquals(0, parents.size());
	}

	@Test
	public void getAllParents() throws Exception {
		final OntologyTermI term = mi.getTermForAccession("MI:0013");
		Assert.assertNotNull(term);

		final Set<OntologyTermI> parents = mi.getAllParents(term);
		Assert.assertNotNull(parents);
		Assert.assertEquals(3, parents.size());
		Assert.assertTrue(parents.contains(new OntologyTermImpl("MI",
				"MI:0045", "experimental interaction detection")));
		Assert.assertTrue(parents.contains(new OntologyTermImpl("MI",
				"MI:0001", "interaction detection method")));
		Assert.assertTrue(parents.contains(new OntologyTermImpl("MI",
				"MI:0000", "molecular interaction")));
	}

	@Test
	public void getAllParents_unknown_accession() throws Exception {
		final OntologyTermImpl term = new OntologyTermImpl("MI", "MI:xxxx",
				"bogus term");

		final Set<OntologyTermI> parents = mi.getAllParents(term);
		Assert.assertNotNull(parents);
		Assert.assertEquals(0, parents.size());
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
		Assert.assertEquals(4, terms.size());
	}

	@Test
	public void getValidTerms_so_large() throws OntologyLoaderException {
		OntologyTermI parent = so.getTermForAccession("SO:0000001");
		Set<OntologyTermI> terms = so.getAllChildren(parent);
		Assert.assertTrue(terms.size() > 10);
	}
}
