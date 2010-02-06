import org.junit.*;
import static org.junit.Assert.*;
import java.io.*;
import java.util.*;

import org.apache.commons.lang.StringEscapeUtils;
import org.biopax.paxtools.impl.level3.*;
import org.biopax.paxtools.io.simpleIO.SimpleReader;
import org.biopax.paxtools.model.*;
import org.biopax.paxtools.model.level3.*;
import org.biopax.validator.utils.OntologyUtils;
import org.springframework.core.io.ClassPathResource;

/**
 *
 * This test does not use a Spring context nor AOP,
 * so no methods and exceptions are intercepted
 * in paxtools.
 *
 *
 * Required JVM OPTIONS: -Xmx2048m
 *
 * @author rodche
 */

public class LibsTest {
	
	static final String L2_SHORT_MET_PATHWAY = "biopax_id_557861_mTor_signaling.owl";
	static final String L3_SHORT_MET_PATHWAY = "biopax3-short-metabolic-pathway.owl";
	   
	/*
	 * starting from biopax-validator v2, it won't use OntologyManager
	 
    //@Test
    public void testOntologyManager() throws Exception {
        System.out.println("test Ontology Manager");

        //load ontologies here!
        // ...  

        Collection<String> names;
        
        // different approach
        names = OntologyUtils.getTermNames(access.getValidTerms("MI:0190", true, false));
        assertTrue(names.contains("phosphorylation"));
        assertTrue(names.contains("phosphorylation reaction"));

        names = OntologyUtils.getTermNames(access.getValidTerms("MI:0120", true, false));
        assertTrue(names.contains("phosphorylated residue"));
        // if the following fails, then it does not include child's children :(
        assertTrue(names.contains("o4&apos;-phospho-tyrosine"));
        
        // Name has a special symbol. Fails? This is a bug in the OntologyManager!
        names = OntologyUtils.getTermNames(access.getValidTerms("MI:0170", true, true));
        assertTrue(names.contains("o4&apos;-phospho-tyrosine"));
        
        // test the problem with XML escape symbols
        String name = "o4'-phospho-tyrosine";
        assertFalse(names.contains(name));
        assertTrue(names.contains(StringEscapeUtils.escapeXml(name)));
        
        // do synonyms include the preferred name?
        OntologyTermI term =access.getTermForAccession("MI:0217");
        names = term.getNameSynonyms();
        assertFalse(names.contains(term.getPreferredName()));
        System.out.println("preferred term: " + term.getPreferredName() 
        		+ " and others: " + names.toString());
        
        // test so.obo problem (endless loop?)
        access = manager.getOntologyAccess("SO");
        OntologyTermI parent = access.getTermForAccession("SO:0000001");
        Collection<OntologyTermI> kids = access.getAllChildren(parent);
    }
    
    //@Test
    public void testOntologyManagerOLS() throws Exception {
        System.out.println("test Ontology Manager");

        OntologyManager manager = 
        	new OntologyUtils(
        			new ClassPathResource("ontologies-remote.xml"));
               
        
        OntologyAccess access = manager.getOntologyAccess("MI");
        Collection<String> names;
        
        OntologyTermI phosphorelationTerm= access.getTermForAccession("MI:0217");
        assertFalse(phosphorelationTerm.getNameSynonyms().isEmpty());
        System.out.println("MI:0217 has synonyms: " 
        		+ phosphorelationTerm.getNameSynonyms().toString());
        
        Set<OntologyTermI> vterms = access.getValidTerms("MI:0190", true, false);
        for(OntologyTermI t : vterms) {
        	if(t.equals(phosphorelationTerm)) {
        		System.out.println(t.getPreferredName() + " has synonyms: " 
                		+ t.getNameSynonyms().toString());
        		assertFalse(t.getNameSynonyms().isEmpty());
        		break;
        	}
        }
        
        assertTrue(vterms.contains(phosphorelationTerm));
        
        names = OntologyUtils.getTermNames(vterms);
        assertTrue(names.contains("phosphorylation reaction"));
        
        assertTrue(names.contains("phosphorylation"));

        names = OntologyUtils.getTermNames(access.getValidTerms("MI:0120", true, false));
        assertTrue(names.contains("phosphorylated residue"));
        // if the following fails, then it does not include child's children :(
        assertTrue(names.contains("o4&apos;-phospho-tyrosine"));
        
        // Name has a special symbol. Fails? This is a bug in the OntologyManager!
        names = OntologyUtils.getTermNames(access.getValidTerms("MI:0170", true, true));
        assertTrue(names.contains("o4&apos;-phospho-tyrosine"));
        
        // test the problem with XML escape symbols
        String name = "o4'-phospho-tyrosine";
        assertFalse(names.contains(name));
        assertTrue(names.contains(StringEscapeUtils.escapeXml(name)));
        
        // do synonyms include the preferred name?
        OntologyTermI term =access.getTermForAccession("MI:0217");
        names = term.getNameSynonyms();
        assertFalse(names.contains(term.getPreferredName()));
        System.out.println("preferred term: " + term.getPreferredName() 
        		+ " and others: " + names.toString());
        
        // test so.obo problem (endless loop?)
        access = manager.getOntologyAccess("SO");
        OntologyTermI parent = access.getTermForAccession("SO:0000001");
        Collection<OntologyTermI> kids = access.getAllChildren(parent);
    }
    
    */
    

    @Test
    public void testBuildPaxtoolsL2ModelSimple() throws FileNotFoundException  {
        System.out.println("with Level2 data");
        InputStream is = new FileInputStream(L2_SHORT_MET_PATHWAY);
        SimpleReader io = new SimpleReader();
        Model model = io.convertFromOWL(is);
        assertNotNull(model);
        assertFalse(model.getObjects().isEmpty());
    }

   
    @Test
    public void testBuildPaxtoolsL3ModelSimple() throws FileNotFoundException {
        System.out.println("with Level3 data");
        InputStream is = new FileInputStream(L3_SHORT_MET_PATHWAY);
        SimpleReader simpleReader = new SimpleReader();
        Model model = simpleReader.convertFromOWL(is);
        assertNotNull(model);
        assertFalse(model.getObjects().isEmpty());
        
        for(BioPAXElement e: model.getObjects()) {
        	if(e instanceof Named) {
        		System.out.println(e + " " + e.getModelInterface().getSimpleName() + 
        				" name:"+ ((Named)e).getStandardName()
        				+ ", displayName: " + ((Named)e).getDisplayName());
        	} else if(e instanceof Xref) {
        		System.out.println(e + " " + e.getModelInterface().getSimpleName() + 
        				" db:"+ ((Xref)e).getDb()
        				+ ", id: " + ((Xref)e).getId());
        	}
        }
        
    }
    
    
    @Test
    public void testIsEquivalentUnificationXref() {
    	Level3Factory factory3 = new Level3FactoryImpl();
    	UnificationXref x1 = factory3.createUnificationXref();
    	x1.setRDFId("x1");
    	x1.addComment("x1");
    	x1.setDb("db");
    	x1.setId("id");
    	UnificationXref x2 = factory3.createUnificationXref();
    	x2.setRDFId("x2");
    	x2.addComment("x2");
    	x2.setDb("db");
    	x2.setId("id");
    	
    	assertTrue(x1.isEquivalent(x2));
    	
    	x2.setRDFId("x1");
    	x2.setDb(null);
    	x2.setId("doesn't matter");
    	
    	assertTrue(x1.isEquivalent(x2));
    	
    }

}