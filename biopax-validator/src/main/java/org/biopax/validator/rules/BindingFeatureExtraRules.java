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

import java.util.HashSet;
import java.util.Set;

import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.BindingFeature;
import org.biopax.validator.api.AbstractRule;
import org.biopax.validator.api.beans.Validation;
import org.biopax.validator.utils.Cluster;
import org.springframework.stereotype.Component;

/**
 * Checks:
 * BindingFeature.bindsTo is 'inverse functional':
 * 
 * @author rodche
 */
@Component
public class BindingFeatureExtraRules extends AbstractRule<Model> {

	public boolean canCheck(Object thing) {
		return thing instanceof Model 
			&& ((Model)thing).getLevel() == BioPAXLevel.L3;
	}

	public void check(final Validation validation, Model model) {
		Set<BindingFeature> bfs = new HashSet<BindingFeature>(
				model.getObjects(BindingFeature.class));

		Cluster<BindingFeature> groupping = new Cluster<BindingFeature>() {
			@Override
			public boolean match(BindingFeature a, BindingFeature b) {
				boolean ab = a.getBindsTo() != null 
					&& b.getBindsTo() != null
						&& a.getBindsTo().isEquivalent(b.getBindsTo());
				
				return !a.isEquivalent(b) && ab;
			}
		};
		
		Set<Set<BindingFeature>> violations 
			= groupping.cluster(bfs, Integer.MAX_VALUE);
		
		// report the error once for each cluster >1
		for (Set<BindingFeature> s : violations) {
			if(s.size() > 1) {
				BindingFeature a = s.iterator().next();
				error(validation, a, "inverse.functional.violated",	false, "bindsTo", a.getBindsTo(), s);
			}
		}
	}
   
}
