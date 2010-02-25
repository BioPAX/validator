package psidev.psi.tools.ontology_manager;

import org.junit.Assert;
import org.junit.Test;
import psidev.psi.tools.ontology_manager.impl.local.LocalOntology;
import psidev.psi.tools.ontology_manager.impl.local.OntologyLoaderException;
import psidev.psi.tools.ontology_manager.interfaces.OntologyAccess;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

/**
 * OntologyManager tester.
 *
 * Author: Florian Reisinger
 * Date: 15-Aug-2007
 */
public class OntologyManagerTest {

    private OntologyManager om;

    public OntologyManagerTest() throws OntologyLoaderException, IOException {
        final String ontoConfig = "ontologies.xml";
        InputStream is = OntologyManager.class.getClassLoader().getResourceAsStream( ontoConfig );
        Assert.assertNotNull( "Could not read ontology configuration file: " + ontoConfig, is );
        OntologyManagerContext.getInstance().setStoreOntologiesLocally(true);
        om = new OntologyManager();
        om.loadOntologies(is);
        is.close();
    }

    @Test
    public void ontologyLoading() {
        Collection<String> ontologyIDs = om.getOntologyIDs();
        Assert.assertEquals( "ontologies.xml specifies only 3 ontology.", 3, ontologyIDs.size() );
        Assert.assertTrue( ontologyIDs.contains( "MOD" ) );
        Assert.assertTrue( ontologyIDs.contains( "SO" ) );
        Assert.assertTrue( ontologyIDs.contains( "MI" ) );

        OntologyAccess oa2 = om.getOntologyAccess( "MOD" );
        Assert.assertNotNull( oa2 );
        Assert.assertTrue( oa2 instanceof LocalOntology);
        
        oa2 = om.getOntologyAccess( "SO" );
        Assert.assertNotNull( oa2 );
        Assert.assertTrue( oa2 instanceof LocalOntology);
        
        oa2 = om.getOntologyAccess( "MI" );
        Assert.assertNotNull( oa2 );
        Assert.assertTrue( oa2 instanceof LocalOntology);
    }
}
