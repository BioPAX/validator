package org.biopax.validator.rules;

import java.util.*;

import org.biopax.paxtools.model.level3.Pathway;
import org.biopax.paxtools.model.level3.PathwayStep;
import org.biopax.paxtools.model.level3.Process;
import org.biopax.validator.api.AbstractRule;
import org.biopax.validator.api.beans.Validation;
import org.springframework.stereotype.Component;

/**
 * Checks: PathwayStep.stepProcess (or stepConversion) - 
 * the same process must be listed in the 
 * Pathway.pathwayComponent property.
 * 
 * @author rodche
 *
 */
@Component
public class PathwayStepProcessesArePathwayComponents extends AbstractRule<PathwayStep> {
	
	public boolean canCheck(Object thing) {
		return thing instanceof PathwayStep 
		/* can be null, e.g., when the check is called 
		 * during a new PathwayStep is added to the model 
		 * but not yet assigned to any Pathway! 
		 * This is ignored.
		 */
		 && ((PathwayStep)thing).getPathwayOrderOf() != null;
	}

	public void check(final Validation validation, PathwayStep step) {
		Pathway pathway = step.getPathwayOrderOf();
		/* can be null, e.g., when the check is called 
		 * during a new PathwayStep is added to the model 
		 * but not yet assigned to any Pathway! This case is ignored.
		 */
		if (pathway != null) {
			Set<Process> pathwayComponents = pathway.getPathwayComponent();
			for (Process stepProcess : step.getStepProcess()) {
				if (!pathwayComponents.contains(stepProcess)) {
					error(validation, step, "component.not.found", false, stepProcess, pathway);
				}
			}
		}
	}
		
}
