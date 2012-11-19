package org.biopax.validator.rules;

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
				return !a.equals(b) && a.getRDFId().equalsIgnoreCase(b.getRDFId());
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
