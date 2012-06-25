package org.biopax.validator.rules;

import org.biopax.paxtools.model.level3.EntityReference;
import org.biopax.paxtools.model.level3.SimplePhysicalEntity;
import org.biopax.validator.impl.AbstractRule;
import org.biopax.validator.utils.BiopaxValidatorUtils;
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
			assert e1.getModelInterface().equals(e2.getModelInterface()); //true as they're both from eref.entityReferenceOf()...
			return  e1.hasEquivalentFeatures(e2) 
					&& e1.hasEquivalentCellularLocation(e2);
			//TODO add 'standardName' for comparison?
		}
	};
	
	
	public void check(EntityReference eref, boolean fix) 
	{

		Set<Set<SimplePhysicalEntity>> clasters 
			= algorithm.cluster(eref.getEntityReferenceOf(), Integer.MAX_VALUE);
	
		// report the error case once per cluster
		for (Set<SimplePhysicalEntity> col : clasters) 
		{
			if(col.size() > 1) {
				SimplePhysicalEntity u = col.iterator().next();
				col.remove(u);
				error(eref, "same.state.entity", false, u,
					BiopaxValidatorUtils.getIdListAsString(col));
			}
		}

	}

	public boolean canCheck(Object thing) {
		return thing instanceof EntityReference;
	}

}
