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
import org.springframework.stereotype.Component;
import org.biopax.paxtools.model.*;
import org.biopax.paxtools.model.level3.Entity;
import org.biopax.paxtools.model.level3.UtilityClass;
import org.biopax.paxtools.model.level3.Xref;
import org.biopax.paxtools.model.level2.entity;
import org.biopax.paxtools.model.level2.utilityClass;
import org.biopax.paxtools.model.level2.xref;
import org.biopax.paxtools.model.level2.externalReferenceUtilityClass;

/**
 * This class advises on instances of too general (top-level) BioPAX classes
 * (Entity, UtilityClass, etc.)
 *
 * @author rodche
 *
 */
@Component
public class NotAllowedInstancesRule extends AbstractRule<BioPAXElement> {

   final Class[] NOT_ALLOWED = {
        Entity.class,
        UtilityClass.class,
        Xref.class,
	xref.class,
	entity.class,
	utilityClass.class,
	externalReferenceUtilityClass.class
    };

    private boolean notAllowed(BioPAXElement bp) {
		for (int i = 0; i < NOT_ALLOWED.length; i++) {
			if (bp.getModelInterface().equals(NOT_ALLOWED[i])) {
				return true; // found
			}
		}
		return false;
	}
    
	public boolean canCheck(Object thing) {
		if(thing instanceof BioPAXElement) {
			return notAllowed((BioPAXElement) thing);
		} else {
			return false;
		}
	}

	public void check(final Validation validation, BioPAXElement thing) {
		error(validation, thing, "not.allowed.element", false, thing.getModelInterface().getSimpleName());
	}
    
}
