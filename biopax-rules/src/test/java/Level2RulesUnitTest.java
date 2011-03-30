import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.biopax.paxtools.impl.level2.Level2FactoryImpl;
import org.biopax.paxtools.io.simpleIO.SimpleExporter;
import org.biopax.paxtools.model.*;
import org.biopax.paxtools.model.level2.*;
import org.biopax.validator.rules.BiochemPathwayStepOneConversionRule;
import org.junit.Before;
import org.junit.Test;

public class Level2RulesUnitTest {

	BioPAXFactory level2;
	SimpleExporter exporter;
	
	final static String TEST_DATA_DIR = Level2RulesUnitTest.class.getResource("").getPath();
	
	void writeExample(String file, Model model) {
    	try {
			exporter.convertToOWL(model, 
				new FileOutputStream(TEST_DATA_DIR + File.separator + file));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
    }
	
	
	@Before
	public void setUp() {
		level2 = new Level2FactoryImpl();	
		exporter = new SimpleExporter(BioPAXLevel.L3);
	}
	
	@Test
	public void testCanCheck() {
		BiochemPathwayStepOneConversionRule rule = new BiochemPathwayStepOneConversionRule();
		pathwayStep pstep = level2.create(pathwayStep.class, "1");
		BioPAXElement bpe = level2.create(conversion.class, "2");
		entity ent = level2.create(protein.class, "3");
		assertFalse(rule.canCheck(null));
		assertFalse(rule.canCheck(ent));
		assertFalse(rule.canCheck(pstep));
		assertFalse(rule.canCheck(bpe));
	}

}
