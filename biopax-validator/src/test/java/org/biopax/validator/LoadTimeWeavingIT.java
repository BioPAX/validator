package org.biopax.validator;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import java.io.*;

import org.biopax.validator.api.Validator;
import org.biopax.validator.api.beans.Validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * This Is an Integration Test.
 *
 * Moreover, this here uses the 'production' application context,
 * i.e., enabling all the AOP aspects and Load-Time Weaving (LTW)! 
 * So, LTW is the one that sometimes makes things complicated...
 *
 * NOTE: 
 *  Surprisingly, but the (sad) fact here is that, 
 *  when using @DirtiesContext annotation with a test method,
 *  it reloads the context, but, in fact, not everything.
 *  E.g., ControlAspect references not the same 'biopaxValidator' bean
 *  instance as the autowired one here... 
 *
 *  So, we do not want to use '@DirtiesContext' in this test class...
 *
 * @author rodche
 */
//@Ignore
@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations = {
	"/META-INF/spring/appContext-loadTimeWeaving.xml",
	"/META-INF/spring/appContext-validator.xml"
})
public class LoadTimeWeavingIT {
	@Autowired
	private Validator biopaxValidator;

	@Autowired
	private ApplicationContext context;

	@Test
	public void validate() throws IOException {
		Resource resource = context
			.getResource("classpath:biopax3-short-metabolic-pathway.owl");
		Validation result = new Validation(new BiopaxIdentifier());
		result.setDescription(resource.getDescription());
		biopaxValidator.importModel(result, resource.getInputStream());
		biopaxValidator.validate(result); // check all rules
		biopaxValidator.getResults().clear(); // clean after itself

		Assertions.assertFalse(result.getError().isEmpty());
	}

	@Test
	public void unknownProperty() {
		Validation validation = new Validation(new BiopaxIdentifier());
		biopaxValidator.importModel(validation, getClass().getResourceAsStream("/testSyntaxErrors.xml"));
		biopaxValidator.getResults().clear(); // clean after itself
		Assertions.assertEquals(1, validation.countErrors(null, null, "unknown.property",
			null, false, false));
		Assertions.assertEquals(0, validation.countErrors(null, null, "syntax.error",
			null, false, false));
	}

	@Test
	public void syntaxErrors() {
		Validation validation = new Validation(new BiopaxIdentifier());
		biopaxValidator.importModel(validation, getClass().getResourceAsStream("/testBiochemPathwayStepOneConversionRule.owl")); //misplaced.step.conversion
		biopaxValidator.getResults().clear(); // clean after itself
		Assertions.assertFalse(validation.getError().isEmpty());
		Assertions.assertEquals(1, validation.countErrors(null, null, "syntax.error",
      null, false, false));
	}

	@Test
	public void clonedUtilityClass() {
		Validation validation = new Validation(new BiopaxIdentifier());
		biopaxValidator.importModel(validation, getClass().getResourceAsStream("/testEvidenceEquivalence.xml"));
		biopaxValidator.validate(validation);
		biopaxValidator.getResults().clear(); // clean after itself
		System.out.println(validation.getError().toString());
		//there are several errors related to CV use,
		Assertions.assertFalse(validation.getError().isEmpty());
		//but - no cloned.utility.class error case
		Assertions.assertEquals(0, validation.countErrors(null, null, "cloned.utility.class",
      null, false, false));
	}

	@Test
	public void memberPhysicalEntityRange() {
		Validation validation = new Validation(new BiopaxIdentifier());
		biopaxValidator.importModel(validation, getClass()
			.getResourceAsStream("/testMemberPhysicalEntityRange.xml"));
		biopaxValidator.validate(validation);
		biopaxValidator.getResults().clear(); // clean after itself
		Assertions.assertEquals(1, validation.countErrors(null, null, "syntax.error",
      null, false, false));
	}
}
