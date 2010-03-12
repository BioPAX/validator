package org.biopax.validator.rules;

import org.biopax.paxtools.model.level3.EntityReference;
import org.biopax.paxtools.model.level3.SimplePhysicalEntity;
import org.biopax.validator.impl.AbstractRule;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Checks PhysicalEntities that reference the same EntityReference 
 * to be in different states (i.e. features on the PhysicalEntity can't be 
 * exactly the same on another PhysicalEntity, e.g. two proteins that 
 * reference the same ProteinReference must have different phosphorylation 
 * states.
 * 
 * @author rodche
 */
@Component
public class EntityReferenceSamePhysicalEntitiesRule extends
		AbstractRule<EntityReference> {

	public void check(EntityReference eref) {
		Set<SimplePhysicalEntity> simplePhysEnts = eref.getEntityReferenceOf();
		for (SimplePhysicalEntity e1 : simplePhysEnts) {
			for (SimplePhysicalEntity e2 : simplePhysEnts) {
				try {
					if (!e1.equals(e2) && e1.hasEquivalentFeatures(e2) 
							&& e1.hasEquivalentCellularLocation(e2)) {
						error(eref, "same.state.entity", e1, e2);
					}
				} catch (Exception e) {
					// no action required
				}
			}
		}
	}

	public boolean canCheck(Object thing) {
		return thing instanceof EntityReference;
	}

	@Override
	public void fix(EntityReference t, Object... values) {	
	}
}
