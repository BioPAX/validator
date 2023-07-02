import org.biopax.psidev.ontology_manager.OntologyAccess;
import org.biopax.psidev.ontology_manager.OntologyManager;
import org.biopax.psidev.ontology_manager.OntologyTermI;
import org.biopax.psidev.ontology_manager.impl.OntologyAccessImpl;
import org.biopax.psidev.ontology_manager.impl.OntologyManagerImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


import java.util.Collection;
import java.util.Properties;

public class OntologyParserTest {
	 
	@Test
	public void ontologyLoading() throws Exception {
		
		final Properties cfg = new Properties();
		cfg.put("MI", "classpath:test-mi.obo");
		cfg.put("MOD", "classpath:test-mod.obo");
		
		OntologyManager manager = new OntologyManagerImpl();
		manager.loadOntologies(cfg);
		
		Collection<String> ontologyIDs = manager.getOntologyIDs();
		Assertions.assertTrue(ontologyIDs.contains("MOD"));
		Assertions.assertTrue(ontologyIDs.contains("MI"));

		OntologyAccess oa2 = manager.getOntology("MOD");
		Assertions.assertNotNull(oa2);
		Assertions.assertTrue(oa2 instanceof OntologyAccessImpl);
		
		OntologyTermI t = oa2.getTermForAccession("MOD:00048");
		Assertions.assertNotNull(t);
		//test that apostrophe is not escaped (-due to a bug in the OBO parser, part of ols-1.18)!
		Assertions.assertTrue(t.getPreferredName().equalsIgnoreCase("O4'-phospho-L-tyrosine"));
		
		oa2 = manager.getOntology("MI");
		Assertions.assertNotNull(oa2);
		Assertions.assertTrue(oa2 instanceof OntologyAccessImpl);
	}
}
