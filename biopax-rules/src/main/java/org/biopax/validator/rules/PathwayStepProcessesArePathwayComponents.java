package org.biopax.validator.rules;

import java.util.*;

import org.biopax.paxtools.model.level3.Pathway;
import org.biopax.paxtools.model.level3.PathwayStep;
import org.biopax.paxtools.model.level3.Process;
import org.biopax.validator.impl.AbstractRule;
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
public final class PathwayStepProcessesArePathwayComponents extends AbstractRule<PathwayStep> {
	
	public boolean canCheck(Object thing) {
		return thing instanceof PathwayStep;
	}

	public void check(PathwayStep step) {
		for(Pathway pathway : step.getPathwayOrdersOf()) {
			Set<Process> pathwayComponents = pathway.getPathwayComponent();
			for (Process stepProcess : step.getStepProcess()) {
				//if (!stepProcess.equals(pathway)) {
					if (!pathwayComponents.contains(stepProcess)) {
						error(step, "component.not.found", stepProcess, pathway);
					}
				//}
			}
		}

	}

	@Override
	public void fix(PathwayStep t, Object... values) {	
	}
		
}
