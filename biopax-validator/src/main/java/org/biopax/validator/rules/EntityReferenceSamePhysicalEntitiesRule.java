package org.biopax.validator.rules;


import org.biopax.paxtools.model.level3.EntityReference;
import org.biopax.paxtools.model.level3.SimplePhysicalEntity;
import org.biopax.validator.AbstractRule;
import org.biopax.validator.api.beans.Validation;
import org.biopax.validator.utils.Cluster;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Checks PhysicalEntities that reference the same EntityReference 
 * to be in different states (i.e. features on the PhysicalEntity can't be 
 * exactly the same on another PhysicalEntity, e.g. two proteins that 
 * reference the same ProteinReference must have different states.
 * 
 * @author rodche
 */
@Component
public class EntityReferenceSamePhysicalEntitiesRule extends
		AbstractRule<EntityReference> 
{

	Cluster<SimplePhysicalEntity> algorithm = new Cluster<SimplePhysicalEntity>() {
		@Override
		public boolean match(SimplePhysicalEntity e1, SimplePhysicalEntity e2) {
			return  e1.hasEquivalentFeatures(e2) 
					&& e1.hasEquivalentCellularLocation(e2);
		}
	};
	
	
	public void check(final Validation validation, EntityReference eref) 
	{

		Set<Set<SimplePhysicalEntity>> clasters 
			= algorithm.cluster(eref.getEntityReferenceOf(), Integer.MAX_VALUE);
	
		// report the error case once per cluster
		for (Set<SimplePhysicalEntity> col : clasters) 
		{
			if(col.size() > 1) {
				SimplePhysicalEntity u = col.iterator().next();
				col.remove(u);
				error(validation, eref, "same.state.entity", false,	u, col);
			}
		}

	}

	public boolean canCheck(Object thing) {
		return thing instanceof EntityReference;
	}

}
