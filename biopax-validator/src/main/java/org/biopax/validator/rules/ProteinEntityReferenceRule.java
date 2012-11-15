package org.biopax.validator.rules;

import org.biopax.paxtools.model.level3.Protein;
import org.biopax.validator.result.Validation;
import org.biopax.validator.impl.AbstractRule;
import org.springframework.stereotype.Component;

/**
 * Checks:
 * Protein.entityReference is not null when its memberPhysicalEntity is null.
 * @author rodche
 */
@Component
public class ProteinEntityReferenceRule extends AbstractRule<Protein> {
	@Override
	public boolean canCheck(Object thing) {
		return thing instanceof Protein;
	}

	@Override
	public void check(final Validation validation, Protein p) {
		if(p.getEntityReference() == null) {
			if(p.getMemberPhysicalEntity().isEmpty()) {
				error(validation, p, "null.entity.reference", false, "ProteinReference");
			} 
		}
	} 
}
