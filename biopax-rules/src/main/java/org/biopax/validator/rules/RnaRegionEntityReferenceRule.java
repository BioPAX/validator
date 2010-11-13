package org.biopax.validator.rules;

import org.biopax.paxtools.model.level3.RnaRegion;
import org.biopax.validator.impl.AbstractRule;
import org.springframework.stereotype.Component;

/**
 * Checks:
 * RnaRegion.entityReference is not null when its memberPhysicalEntity is null.
 * @author rodche
 */
@Component
public class RnaRegionEntityReferenceRule extends AbstractRule<RnaRegion> {
	@Override
	public boolean canCheck(Object thing) {
		return thing instanceof RnaRegion;
	}

	@Override
	public void check(RnaRegion sm, boolean fix) {
		if(sm.getEntityReference() == null) {
			if(sm.getMemberPhysicalEntity().isEmpty()) {
				error(sm, "null.entity.reference", false, "RnaRegionReference");
			} 
		}
	} 
}
