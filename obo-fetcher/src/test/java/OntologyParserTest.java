import static org.junit.Assert.*;

import org.biopax.psidev.ontology_manager.OntologyAccess;
import org.biopax.psidev.ontology_manager.OntologyManager;
import org.biopax.psidev.ontology_manager.OntologyTermI;
import org.biopax.psidev.ontology_manager.impl.OntologyAccessImpl;
import org.biopax.psidev.ontology_manager.impl.OntologyLoaderException;
import org.biopax.psidev.ontology_manager.impl.OntologyManagerImpl;
import org.junit.*;


import java.util.Collection;
import java.util.Properties;

public class OntologyParserTest {
	 
	@Test
	public void ontologyLoading() throws OntologyLoaderException {
		
		final Properties cfg = new Properties();
		cfg.put("MI", "classpath:mi.obo");
		cfg.put("MOD", "classpath:mod.obo");
		
		OntologyManager manager = new OntologyManagerImpl(cfg);
		
		Collection<String> ontologyIDs = manager.getOntologyIDs();
		assertTrue(ontologyIDs.contains("MOD"));
		assertTrue(ontologyIDs.contains("MI"));

		OntologyAccess oa2 = manager.getOntology("MOD");
		assertNotNull(oa2);
		assertTrue(oa2 instanceof OntologyAccessImpl);
		
		OntologyTermI t = oa2.getTermForAccession("MOD:00048");
		assertNotNull(t);
		//test that apostrophe is not escaped (-due to a bug in the OBO parser, part of ols-1.18)!
		assertTrue(t.getPreferredName().equalsIgnoreCase("O4'-phospho-L-tyrosine"));
		
		oa2 = manager.getOntology("MI");
		assertNotNull(oa2);
		assertTrue(oa2 instanceof OntologyAccessImpl);
	}
}
