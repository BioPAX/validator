package org.biopax.validator.rules;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.SimplePhysicalEntity;
import org.biopax.validator.impl.AbstractRule;
import org.biopax.validator.utils.BiopaxValidatorUtils;
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

	public void check(Model model, boolean fix) {
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
							&& !BiopaxValidatorUtils.namesInCommon(a, b).isEmpty();
					}
			};
			
			Collection<Set<SimplePhysicalEntity>> sharedNamesClusters = 
				groupping.groupByEquivalence(peers.toArray(new SimplePhysicalEntity[]{}),
						BiopaxValidatorUtils.maxErrors);
				
			// report the error once for each cluster
			for (Set<SimplePhysicalEntity> s : sharedNamesClusters) {
				SimplePhysicalEntity a = s.iterator().next();
				error(a, "diff.kind.same.name", BiopaxValidatorUtils.getIdListAsString(s));
			}
	}

	
	public boolean canCheck(Object thing) {
		return thing instanceof Model
			&& ((Model)thing).getLevel()==BioPAXLevel.L3;
	}

}
