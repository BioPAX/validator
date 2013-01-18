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

import org.biopax.paxtools.model.level3.BindingFeature;
import org.biopax.validator.api.AbstractRule;
import org.biopax.validator.api.beans.Validation;
import org.springframework.stereotype.Component;

/**
 * Checks:
 * BindingFeature.bindsTo is 'symmetrical'.
 * 
 * @author rodche
 */
@Component
public class BindingFeatureSymmetricRule extends AbstractRule<BindingFeature> {

	public boolean canCheck(Object thing) {
		return thing instanceof BindingFeature
			&& ((BindingFeature)thing).getBindsTo() != null;
	}

	public void check(final Validation validation, BindingFeature thing) {
		BindingFeature to = thing.getBindsTo();
		if (to != null) {
			if (to.getBindsTo() == null || !thing.equals(to.getBindsTo())) 
			{	
				error(validation, thing, "symmetric.violated", validation.isFix(), 
						"bindsTo", thing.getBindsTo());
				if(validation.isFix()) {
					to.setBindsTo(thing);
				}
			}
		}
	}
	
}
