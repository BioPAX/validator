package org.biopax.validator.rules;

import org.biopax.paxtools.model.level3.Conversion;
import org.biopax.paxtools.model.level3.BiochemicalPathwayStep;
import org.biopax.paxtools.model.level3.Process;
import org.biopax.validator.api.AbstractRule;
import org.biopax.validator.api.beans.Validation;
import org.springframework.stereotype.Component;

/**
 * Checks: BiochemicalPathwayStep - only one conversion interaction 
 * can be ordered at a time, though multiple catalysis or modulation 
 * instances can be part of the step.
 * 
 * @author rodche
 *
 */
@Component
public class BiochemPathwayStepOneConversionRule extends AbstractRule<BiochemicalPathwayStep> {
	
	public boolean canCheck(Object thing) {
		return thing instanceof BiochemicalPathwayStep;
	}

	public void check(final Validation validation, BiochemicalPathwayStep step) {
		if (step.getStepProcess() != null) {
			for (Process process : step.getStepProcess()) {
				if (process instanceof Conversion) {
					error(validation, step, "misplaced.step.conversion", false, process);
				}
			}
		}
	}	
}
