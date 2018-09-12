package org.biopax.validator.rules;

/*
 *
 */

import org.biopax.paxtools.model.level3.Dna;
import org.biopax.validator.api.AbstractRule;
import org.biopax.validator.api.beans.Validation;
import org.springframework.stereotype.Component;

/**
 * Checks:
 * DnaRegion.entityReference is not null when its memberPhysicalEntity is null.
 * @author rodche
 */
@Component
public class DnaEntityReferenceRule extends AbstractRule<Dna> {

	public boolean canCheck(Object thing) {
		return thing instanceof Dna;
	}

	public void check(final Validation validation, Dna sm) {
		if(sm.getEntityReference() == null) {
			if(sm.getMemberPhysicalEntity().isEmpty()) {
				error(validation, sm, "null.entity.reference", false, "DnaReference");
			} 
		}
	} 
}
