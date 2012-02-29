import static org.junit.Assert.*;

import java.util.*;

import org.junit.*;
import org.junit.runner.RunWith;
import java.io.*;

import org.biopax.paxtools.impl.level3.Level3FactoryImpl;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.*;
import org.biopax.validator.result.*;
import org.biopax.validator.Validator;
import org.biopax.validator.rules.XrefRule;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * This Is an Integration Test.
 *
 * Moreover, this here uses the 'production' application context,
 * i.e., enabling all the AOP aspects and Load-Time Weaving (LTW)! 
 * So, LTW is the one that sometimes makes things complicated 
 * (I believe, - due to Spring's bug...)
 *
 * NOTE: 
 *  Surprisingly, but the (sad) fact here is that, 
 *  when using @DirtiesContext annotation with a test method,
 *  it reloads the context, but, in fact, not everything.
 *  E.g., ControlAspect references not the same 'validator' bean
 *  instance as the autowired one here... 
 *  
 *  So, we do not want to use '@DirtiesContext' in this test class... 
 *
 * TODO Report the issue to Springsource
 *
 * @author rodche
 */
//@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:validator-aop-context.xml"})
public class AOPAspectJLTWIntegrationTest {
    @Autowired
    Validator validator;
    
    @Autowired
    ApplicationContext context;

    Level3FactoryImpl factory3 = (Level3FactoryImpl) BioPAXLevel.L3.getDefaultFactory();
    
    @Test
    public void testValidator() throws IOException {
    	Resource resource = context
    		.getResource("classpath:biopax3-short-metabolic-pathway.owl");
    	Validation result = new Validation();
    	result.setDescription(resource.getDescription());
    	validator.importModel(result, resource.getInputStream());
        validator.validate(result); // check all rules
        System.out.println(result.getError().toString());
        assertFalse(result.getError().isEmpty());
        validator.getResults().clear(); // clean after itself
    }

    @Test
    public void testRange1() {
        Evidence ev = (Evidence) factory3.create(Evidence.class, "1");
        EvidenceCodeVocabulary ec = factory3.create(EvidenceCodeVocabulary.class, "2");
        ev.addEvidenceCode(ec);
        /**
         * TODO check correct types are actually in such Set,
         * because the following works but shouldn't!
         */
        ControlledVocabulary cv = (CellVocabulary) factory3.create(CellVocabulary.class, "3");
        Set<ControlledVocabulary> set = new HashSet<ControlledVocabulary>(); // not Set<EvidenceCodeVocabulary>
        set.add(ec);
        set.add(cv);
        //ev.setEvidenceCode(set); // compile-time error       
    }
   

    /**
     * This tests rule's action via AOP
     * (XrefRule is just one example)
     * 
     * For this to work, AOP is to be engaged, and 
     * the rule should be set 'postModelOnly=false'
     * 
     */
    @Test
    public void testXrefRuleAOP() {
        XrefRule rule =  (XrefRule) context.getBean("xrefRule");
        rule.setPostModelOnly(false); // enable it in the ControlAspect (AOP)
        
        UnificationXref x = factory3.create(UnificationXref.class, "Illegal-Xref-Db");
    	// One cannot add an element to the model without setting the ID first,
        // and AOP validation also ignores any issues with such 'raw' elements.
    	// In fact, with and without rdf:ID - are two different objects, e.g., 
        // in terms of a Collection<BioPAXElement> 'contains' method...
    	// (this is because BioPAXElement's hashCode and equals have custom implementation in the paxtools-core...)
    	
        // adding a wrong String value here is checked, 
    	// but the validator won't report any error at this moment,
    	// because 'x' is not associated with any validation results yet.
    	//x.setDb("ILLEGALDB");
    	
    	// create a model and register in the validator
    	Model model = factory3.createModel();
    	Validation validation = new Validation("test");
    	validator.associate(model, validation);
    	validator.indirectlyAssociate(model, x);
        
    	//Let's add the xref with the illlegal 'db' value to the model:
       	model.add(x); // x has illegal 'db', but no error is being reported, because db==null
       	x.setDb("ILLEGALDB"); // now error should have been reported
       	
       	x.setDb("ENTREZGENE"); // synonym, preferred name is "Entrez Gene", should not add errors...
       	
       	// reset rule's default (to prevent effect on other tests)
       	rule.setPostModelOnly(true);   
       	validator.getResults().remove(validation);
       	
       	
       	// check the error was caught
       	Collection<ErrorType> errors = validation.getError();
       	
       	assertFalse(errors.isEmpty());
       	
       	assertNotNull(validation.findErrorType("unknown.db", Behavior.ERROR));
       	
       	assertTrue(errors.size()==1);
    }

    
    /**
     * This is to test one very specific syntax rule, 
     * which is not a standard one that extends 
     * AbstractRule interface.
     * 
     * This should be tested here because the rule is kicked off
     * by the ControlAspect that only works when AOP/LTW is active.
     * 
     * Name (e.g., displayName, standardName, and Name) duplicates 
     * can be truly found only during model is being read. 
     * Because, after it's read, PaxTools itself creates such duplicates, i.e.: 
     * setDisplayName method also adds to the 'name' property
     * (by calling addName)... However, this gets fixed (filtered) 
     * at the model export time...
     * 
     * @throws IOException
     */
    @Test
    public void testDuplicateNamesImport() throws IOException {
    	Validation validation = new Validation();
    	validation.setFix(true);
    	validator.importModel(validation, getClass()
    			.getResourceAsStream("testDuplicateNamesImport.xml"));
    	ErrorType error = validation.findErrorType("duplicate.names", Behavior.WARNING);
    	assertNotNull(error);
    }
    
    
    @Test
    public void testSyntaxErrors() throws IOException {
    	Validation validation = new Validation();
    	//validation.setFix(true);
    	validator.importModel(validation, getClass()
    			.getResourceAsStream("testSyntaxErrors.xml"));    	
    	ErrorType error = validation.findErrorType("unknown.property", Behavior.ERROR);
    	assertNotNull(error);
    }
    
    @Test
    public void testClonedUtilityClass() throws IOException {
    	Validation validation = new Validation();
    	validator.importModel(validation, getClass()
    			.getResourceAsStream("testEvidenceEquivalence.xml")); 
    	validator.validate(validation);
    	ErrorType error = validation.findErrorType("cloned.utility.class", Behavior.WARNING);
    	assertNull(error);
    }
    
    @Test
    public void testMemberPhysicalEntityRange() throws IOException {
    	Validation validation = new Validation();
    	validator.importModel(validation, getClass()
    			.getResourceAsStream("testMemberPhysicalEntityRange.xml")); 
    	validator.validate(validation);
    	ErrorType error = validation.findErrorType("syntax.error", Behavior.ERROR);
    	assertNotNull(error);
    }
}