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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.biopax.paxtools.controller.ModelUtils;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.UtilityClass;
import org.biopax.validator.api.AbstractRule;
import org.biopax.validator.api.beans.Validation;
import org.biopax.validator.utils.Cluster;
import org.springframework.stereotype.Component;

/**
 * Checks equivalent (duplicated) UtilityClass instances.
 * It collects/reports all duplicates. 
 * 
 * @author rodche
 */
@Component
public class ClonedUtilityClassRule extends	AbstractRule<Model> {
	
	public void check(final Validation validation, Model model) {
		Cluster<UtilityClass> algorithm = new Cluster<UtilityClass>() {
			@Override
			public boolean match(UtilityClass a, UtilityClass b) {
				return !a.equals(b) && a.isEquivalent(b);
			}
		};
		
		Set<Set<UtilityClass>> clusters 
			= algorithm.cluster(model.getObjects(UtilityClass.class), Integer.MAX_VALUE);
		
		Map<UtilityClass, UtilityClass> replacementMap = new HashMap<UtilityClass, UtilityClass>();
		
		// report the error once for each cluster
		for (Set<UtilityClass> clones : clusters) {
			if(clones.size() < 2)
				continue; //skip unique individuals
			
			UtilityClass first = clones.iterator().next();
			clones.remove(first); // pop the first element from the clones collection

			if(validation.isFix()) {		
				// set "fixed" in advance... fix below
				error(validation, first, "cloned.utility.class", 
					true, clones, first.getModelInterface().getSimpleName());
				
				for(UtilityClass clone : clones)
					replacementMap.put(clone, first);
				
			} else {
				// report the problem (not fixed)
				error(validation, first, "cloned.utility.class", 
					false, clones, first.getModelInterface().getSimpleName());
			}
		}
		
		if(validation.isFix())
			ModelUtils.replace(model, replacementMap);
		
	}

	public boolean canCheck(Object thing) {
		return thing instanceof Model 
			&& ((Model)thing).getLevel() == BioPAXLevel.L3;
	}	
}
