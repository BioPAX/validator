package org.biopax.validator.rules;

/*
 * #%L
 * BioPAX Validator
 * %%
 * Copyright (C) 2008 - 2013 University of Toronto (baderlab.org) and Memorial Sloan-Kettering Cancer Center (cbio.mskcc.org)
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

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
