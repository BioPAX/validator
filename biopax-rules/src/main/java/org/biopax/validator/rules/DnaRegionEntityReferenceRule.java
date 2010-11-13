package org.biopax.validator.rules;

import org.biopax.paxtools.model.level3.DnaRegion;
import org.biopax.validator.impl.AbstractRule;
import org.springframework.stereotype.Component;

/**
 * Checks:
 * DnaRegion.entityReference is not null when its memberPhysicalEntity is null.
 * @author rodche
 */
@Component
public class DnaRegionEntityReferenceRule extends AbstractRule<DnaRegion> {
	@Override
	public boolean canCheck(Object thing) {
		return thing instanceof DnaRegion;
	}

	@Override
	public void check(DnaRegion sm, boolean fix) {
		if(sm.getEntityReference() == null) {
			if(sm.getMemberPhysicalEntity().isEmpty()) {
				error(sm, "null.entity.reference", false, "DnaRegionReference");
			} 
		}
	} 
}
