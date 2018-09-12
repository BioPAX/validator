package org.biopax.validator.rules;

/*
 *
 */

import org.biopax.paxtools.model.level3.DnaRegion;
import org.biopax.validator.api.AbstractRule;
import org.biopax.validator.api.beans.Validation;
import org.springframework.stereotype.Component;

/**
 * Checks:
 * DnaRegion.entityReference is not null when its memberPhysicalEntity is null.
 * @author rodche
 */
@Component
public class DnaRegionEntityReferenceRule extends AbstractRule<DnaRegion> {

	public boolean canCheck(Object thing) {
		return thing instanceof DnaRegion;
	}

	public void check(final Validation validation, DnaRegion sm) {
		if(sm.getEntityReference() == null) {
			if(sm.getMemberPhysicalEntity().isEmpty()) {
				error(validation, sm, "null.entity.reference", false, "DnaRegionReference");
			} 
		}
	} 
}
