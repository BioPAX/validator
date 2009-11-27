package tests;

import static org.junit.Assert.*;
import org.biopax.paxtools.impl.level2.Level2FactoryImpl;
import org.biopax.paxtools.model.*;
import org.biopax.paxtools.model.level2.*;
import org.biopax.validator.rules.BiochemPathwayStepOneConversionRule;
import org.junit.Before;
import org.junit.Test;

public class Level2RulesUnitTest {

	Level2Factory level2;
	
	@Before
	public void setUp() {
		level2 = new Level2FactoryImpl();		
	}
	
	@Test
	public void testCanCheck() {
		BiochemPathwayStepOneConversionRule rule = new BiochemPathwayStepOneConversionRule();
		pathwayStep pstep = level2.createPathwayStep();
		BioPAXElement bpe = level2.createConversion();
		entity ent = level2.createProtein();
		assertFalse(rule.canCheck(null));
		assertFalse(rule.canCheck(ent));
		assertFalse(rule.canCheck(pstep));
		assertFalse(rule.canCheck(bpe));
	}

}
