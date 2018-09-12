package org.biopax.validator.rules;

/*
 *
 */

import org.biopax.paxtools.model.level3.RnaRegion;
import org.biopax.validator.api.AbstractRule;
import org.biopax.validator.api.beans.Validation;
import org.springframework.stereotype.Component;

/**
 * Checks:
 * RnaRegion.entityReference is not null when its memberPhysicalEntity is null.
 * @author rodche
 */
@Component
public class RnaRegionEntityReferenceRule extends AbstractRule<RnaRegion> {

	public boolean canCheck(Object thing) {
		return thing instanceof RnaRegion;
	}

	public void check(final Validation validation, RnaRegion sm) {
		if(sm.getEntityReference() == null) {
			if(sm.getMemberPhysicalEntity().isEmpty()) {
				error(validation, sm, "null.entity.reference", false, "RnaRegionReference");
			} 
		}
	} 
}
