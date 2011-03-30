import static org.junit.Assert.*;

import org.junit.*;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import psidev.ontology_manager.Ontology;
import psidev.ontology_manager.OntologyManager;
import psidev.ontology_manager.impl.OntologyImpl;
import psidev.ontology_manager.impl.OntologyLoaderException;
import psidev.ontology_manager.impl.OntologyManagerContext;
import psidev.ontology_manager.impl.OntologyManagerImpl;

import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/*
SO:
http://obo.cvs.sourceforge.net/*checkout* /obo/obo/ontology/genomic-proteomic/so.obo
http://berkeleybop.org/ontologies/obo-all/sequence/sequence.obo
http://song.cvs.sourceforge.net/viewvc/song/ontology/so.obo?revision=1.283
file://...
MI:
http://psidev.cvs.sourceforge.net/viewvc/*checkout* /psidev/psi/mi/rel25/data/psi-mi25.obo
http://berkeleybop.org/ontologies/obo-all/psi-mi/psi-mi.obo
http://psidev.cvs.sourceforge.net/viewvc/psidev/psi/mi/rel25/data/psi-mi25.obo?revision=1.58
MOD:
http://psidev.cvs.sourceforge.net/viewvc/psidev/psi/mod/data/PSI-MOD.obo
http://berkeleybop.org/ontologies/obo-all/psi-mod/psi-mod.obo
http://psidev.cvs.sourceforge.net/viewvc/psidev/psi/mod/data/PSI-MOD.obo?revision=1.23
 */
public class OntologyParserTest {

	private static final ResourceLoader LOADER = new DefaultResourceLoader();
	 
	@Test
	public void ontologyLoading() throws OntologyLoaderException {
		OntologyManagerContext.getInstance().setStoreOntologiesLocally(true);
		
		final Map<String, Resource> cfg = new HashMap<String, Resource>();
		cfg.put("SO", LOADER.getResource("http://song.cvs.sourceforge.net/viewvc/song/ontology/so.obo")); //?revision=1.283
		cfg.put("MI", LOADER.getResource("http://psidev.cvs.sourceforge.net/viewvc/psidev/psi/mi/rel25/data/psi-mi25.obo?revision=1.58"));
		cfg.put("MOD", LOADER.getResource("http://psidev.cvs.sourceforge.net/viewvc/psidev/psi/mod/data/PSI-MOD.obo?revision=1.23"));
		
		OntologyManager manager = new OntologyManagerImpl(cfg);
		
		Collection<String> ontologyIDs = manager.getOntologyIDs();
		assertTrue(ontologyIDs.contains("MOD"));
		assertTrue(ontologyIDs.contains("SO"));
		assertTrue(ontologyIDs.contains("MI"));

		Ontology oa2 = manager.getOntology("MOD");
		assertNotNull(oa2);
		assertTrue(oa2 instanceof OntologyImpl);

		oa2 = manager.getOntology("SO");
		Assert.assertNotNull(oa2);
		Assert.assertTrue(oa2 instanceof OntologyImpl);

		oa2 = manager.getOntology("MI");
		assertNotNull(oa2);
		assertTrue(oa2 instanceof OntologyImpl);
	}
}
