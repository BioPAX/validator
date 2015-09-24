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

import java.util.Set;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.validator.api.AbstractRule;
import org.biopax.validator.api.beans.Validation;
import org.biopax.validator.utils.Cluster;
import org.springframework.stereotype.Component;

/**
 * Checks for "duplicate" IDs - when they're compared ignoring case.
 * 
 * @author rodche
 */
@Component
public class DuplicateIdCaseInsensitiveRule extends	AbstractRule<Model> {
	
	public void check(final Validation validation, Model model) {		
		Cluster<BioPAXElement> algorithm = new Cluster<BioPAXElement>() {
			@Override
			public boolean match(BioPAXElement a, BioPAXElement b) {
				return !a.equals(b) && a.getUri().equalsIgnoreCase(b.getUri());
			}
		};
		
		Set<Set<BioPAXElement>> clasters 
			= algorithm.cluster(model.getObjects(), Integer.MAX_VALUE);
		
		// report the error once for each cluster
		for (Set<BioPAXElement> duplicates : clasters) {
			if(duplicates.size() > 1) {
				BioPAXElement u = duplicates.iterator().next();
				duplicates.remove(u); // keep the first element
				error(validation, u, "duplicate.id.ignoringcase", false, 
					duplicates, u.getModelInterface().getSimpleName());
			}
		}
	}

	public boolean canCheck(Object thing) {
		return thing instanceof Model;
	}

}
