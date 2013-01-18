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
