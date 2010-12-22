package org.biopax.validator.rules;

import java.util.Collection;

import org.apache.commons.collections15.set.CompositeSet;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.validator.impl.AbstractRule;
import org.biopax.validator.utils.BiopaxValidatorUtils;
import org.biopax.validator.utils.Cluster;
import org.springframework.stereotype.Component;

/**
 * Checks for "duplicate" IDs - when they're compared ignoring case.
 * 
 * @author rodche
 */
@Component
public class DuplicateIdCaseInsensitiveRule extends	AbstractRule<Model> {
	
	public void check(Model model, boolean fix) {
		BioPAXElement[] peers = 
				model.getObjects().toArray(new BioPAXElement[]{});
		
		Cluster<BioPAXElement> algorithm = new Cluster<BioPAXElement>() {
			@Override
			public boolean match(BioPAXElement a, BioPAXElement b) {
				return !a.equals(b) && a.getRDFId().equalsIgnoreCase(b.getRDFId());
			}
		};
		
		CompositeSet<BioPAXElement> clasters 
			= algorithm.groupByEquivalence(peers, Integer.MAX_VALUE);
		
		// report the error once for each cluster
		for (Collection<BioPAXElement> duplicates : clasters.getCollections()) {
			BioPAXElement u = duplicates.iterator().next();
			duplicates.remove(u); // keep the first element
			error(u, "duplicate.id.ignoringcase", fix, 
					BiopaxValidatorUtils.getIdListAsString(duplicates), 
						u.getModelInterface().getSimpleName());
			if(fix) {
				// TODO
			}
		}
	}

	public boolean canCheck(Object thing) {
		return thing instanceof Model;
	}

}
