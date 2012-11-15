package org.biopax.validator.rules;

import org.biopax.paxtools.model.level3.Rna;
import org.biopax.validator.result.Validation;
import org.biopax.validator.impl.AbstractRule;
import org.springframework.stereotype.Component;

/**
 * Checks:
 * Rna.entityReference is not null when its memberPhysicalEntity is null.
 * @author rodche
 */
@Component
public class RnaEntityReferenceRule extends AbstractRule<Rna> {
	@Override
	public boolean canCheck(Object thing) {
		return thing instanceof Rna;
	}

	@Override
	public void check(final Validation validation, Rna sm) {
		if(sm.getEntityReference() == null) {
			if(sm.getMemberPhysicalEntity().isEmpty()) {
				error(validation, sm, "null.entity.reference", false, "RnaReference");
			} 
		}
	} 
}
