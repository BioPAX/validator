package org.biopax.validator.rules;

/*
 *
 */

import org.biopax.paxtools.model.level3.Rna;
import org.biopax.validator.api.AbstractRule;
import org.biopax.validator.api.beans.Validation;
import org.springframework.stereotype.Component;

/**
 * Checks:
 * Rna.entityReference is not null when its memberPhysicalEntity is null.
 * @author rodche
 */
@Component
public class RnaEntityReferenceRule extends AbstractRule<Rna> {

	public boolean canCheck(Object thing) {
		return thing instanceof Rna;
	}

	public void check(final Validation validation, Rna sm) {
		if(sm.getEntityReference() == null) {
			if(sm.getMemberPhysicalEntity().isEmpty()) {
				error(validation, sm, "null.entity.reference", false, "RnaReference");
			} 
		}
	} 
}
