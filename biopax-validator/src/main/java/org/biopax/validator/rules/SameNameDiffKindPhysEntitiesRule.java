package org.biopax.validator.rules;


import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections15.CollectionUtils;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.SimplePhysicalEntity;
import org.biopax.validator.AbstractRule;
import org.biopax.validator.api.beans.Validation;
import org.biopax.validator.utils.Cluster;
import org.springframework.stereotype.Component;

/**
 * Checks that defferent kind physical entities 
 * (using different EntityReference) do not share
 * names.
 * 
 * @author rodche
 */
@Component
public class SameNameDiffKindPhysEntitiesRule extends
		AbstractRule<Model> {

	public void check(final Validation validation, Model model) {
			Set<SimplePhysicalEntity> peers = new HashSet<SimplePhysicalEntity>(
				model.getObjects(SimplePhysicalEntity.class));
			
			Cluster<SimplePhysicalEntity> groupping =
				new Cluster<SimplePhysicalEntity>() {
					@Override
					public boolean match(SimplePhysicalEntity a,
							SimplePhysicalEntity b) {
						return !a.equals(b) 
							&& a.getEntityReference() != null
							&& !a.getName().isEmpty() && !b.getName().isEmpty()
							&& !a.getEntityReference().isEquivalent(b.getEntityReference())
							&& CollectionUtils.containsAny(a.getName(), b.getName());
					}
			};
			
			Set<Set<SimplePhysicalEntity>> sharedNamesClusters = 
				groupping.cluster(peers, Integer.MAX_VALUE);
				
			// report the error once for each cluster
			for (Set<SimplePhysicalEntity> sharedNames : sharedNamesClusters) 
			{
				if(sharedNames.size() > 1) {
					SimplePhysicalEntity a = sharedNames.iterator().next();
					error(validation, a, "diff.kind.same.name",	false, sharedNames);
				}
			}
	}

	
	public boolean canCheck(Object thing) {
		return thing instanceof Model
			&& ((Model)thing).getLevel()==BioPAXLevel.L3;
	}

}
