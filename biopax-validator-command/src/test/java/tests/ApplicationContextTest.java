package tests;
import static org.junit.Assert.*;
import java.util.*;

import org.junit.*;
import org.junit.runner.RunWith;
import java.io.*;

import org.biopax.paxtools.impl.level3.Level3FactoryImpl;
import org.biopax.paxtools.model.level3.*;
import org.biopax.validator.Validator;
import org.biopax.validator.result.Validation;
import org.biopax.validator.utils.XrefHelper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author rodche
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:validator-context.xml"})
public class ApplicationContextTest {
    Level3FactoryImpl factory3;
    static final String TEST_PATHWAY = "biopax3-short-metabolic-pathway.owl";
      
    @Autowired
    Validator validator;
    
    @Autowired
    XrefHelper xrefHelper;
    
    @Autowired
    ApplicationContext context;
    
    @Before
    public void setUp() {
        System.out.println("Running " + this.getClass().getName());
        factory3 = new Level3FactoryImpl();
    }

    @Test
    public void testValidator() throws IOException {
        System.out.println("test Rules Default Check");
    	Resource resource = context.getResource(TEST_PATHWAY);
    	Validation result = new Validation();
    	result.setDescription(resource.getDescription());
    	validator.importModel(result, resource.getInputStream());
        validator.validate(result); // check all rules
        assertFalse(result.getError().isEmpty());
    }

    @Test
    public void testRange1() {
        System.out.println("test Range (1)");
        Evidence ev = (Evidence) factory3.createEvidence();
        EvidenceCodeVocabulary ec = factory3.createEvidenceCodeVocabulary();
        ev.addEvidenceCode(ec);
        /**
         * TODO check correct types are actually in such Set,
         * because the following works but shouldn't!
         */
        ControlledVocabulary cv = (CellVocabulary) factory3.createCellVocabulary();
        Set<ControlledVocabulary> set = new HashSet<ControlledVocabulary>(); // not Set<EvidenceCodeVocabulary>
        set.add(ec);
        set.add(cv);
        //ev.setEvidenceCode(set); // compile-time error       
    }
    
}
