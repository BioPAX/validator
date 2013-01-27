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

import org.biopax.validator.api.AbstractRule;
import org.biopax.validator.api.beans.Validation;
import org.biopax.paxtools.model.*;
import org.biopax.paxtools.model.level3.Control;
import org.biopax.paxtools.model.level3.Conversion;
import org.biopax.paxtools.model.level3.EntityFeature;
import org.biopax.paxtools.model.level3.Interaction;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.springframework.stereotype.Component;
import org.biopax.paxtools.model.level2.conversion;
import org.biopax.paxtools.model.level2.physicalEntity;

/**
 * This class warns on instances of too general (top-level) BioPAX classes
 *
 * @author rodche
 *
 */
@Component
public class NotAdvisedInstancesRule extends AbstractRule<BioPAXElement> {

    final Class[] NOT_ADVISED = {
    	Control.class, 
    	Conversion.class,
        Interaction.class,
    	EntityFeature.class, 
    	PhysicalEntity.class, 
    	physicalEntity.class,
    	conversion.class,
    };
    
    private boolean notAllowed(Object o) {
		if (o instanceof BioPAXElement) {
			for (int i = 0; i < NOT_ADVISED.length; i++) {
				if (((BioPAXElement) o).getModelInterface().equals(
						NOT_ADVISED[i])) {
					return true; // found
				}
			}
		}
		return false;
	}
    
	public boolean canCheck(Object thing) {
		return notAllowed(thing);
	}

	public void check(final Validation validation, BioPAXElement thing) {
		error(validation, thing, "not.specific.element", false, thing.getModelInterface().getSimpleName());
	}

}
