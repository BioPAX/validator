import static org.junit.Assert.*;

import org.biopax.psidev.ontology_manager.Ontology;
import org.biopax.psidev.ontology_manager.OntologyManager;
import org.biopax.psidev.ontology_manager.OntologyTermI;
import org.biopax.psidev.ontology_manager.impl.OntologyImpl;
import org.biopax.psidev.ontology_manager.impl.OntologyLoaderException;
import org.biopax.psidev.ontology_manager.impl.OntologyManagerContext;
import org.biopax.psidev.ontology_manager.impl.OntologyManagerImpl;
import org.junit.*;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;


import java.util.Collection;
import java.util.Properties;

public class OntologyParserTest {
	 
	@Test
	public void ontologyLoading() throws OntologyLoaderException {
		OntologyManagerContext.getInstance().setStoreOntologiesLocally(true);
		
		final Properties cfg = new Properties();
		cfg.put("SO", "http://song.cvs.sourceforge.net/viewvc/song/ontology/so.obo?revision=1.310");
		cfg.put("MI", "http://psidev.cvs.sourceforge.net/viewvc/*checkout*/psidev/psi/mi/rel25/data/psi-mi25.obo?revision=1.60");
		cfg.put("MOD", "http://psidev.cvs.sourceforge.net/viewvc/*checkout*/psidev/psi/mod/data/PSI-MOD.obo?revision=1.24");
		
		OntologyManager manager = new OntologyManagerImpl(cfg);
		
		Collection<String> ontologyIDs = manager.getOntologyIDs();
		assertTrue(ontologyIDs.contains("MOD"));
		assertTrue(ontologyIDs.contains("SO"));
		assertTrue(ontologyIDs.contains("MI"));

		Ontology oa2 = manager.getOntology("MOD");
		assertNotNull(oa2);
		assertTrue(oa2 instanceof OntologyImpl);
		
		OntologyTermI t = oa2.getTermForAccession("MOD:00048");
		assertNotNull(t);
		//test that apostrophe is not escaped (-due to a bug in the OBO parser, part of ols-1.18)!
		assertTrue(t.getPreferredName().equalsIgnoreCase("O4'-phospho-L-tyrosine"));
		

		oa2 = manager.getOntology("SO");
		Assert.assertNotNull(oa2);
		Assert.assertTrue(oa2 instanceof OntologyImpl);

		oa2 = manager.getOntology("MI");
		assertNotNull(oa2);
		assertTrue(oa2 instanceof OntologyImpl);
	}
}
