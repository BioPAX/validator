package org.biopax.validator.rules;

import org.biopax.paxtools.model.level3.Dna;
import org.biopax.validator.impl.AbstractRule;
import org.springframework.stereotype.Component;

/**
 * Checks:
 * DnaRegion.entityReference is not null when its memberPhysicalEntity is null.
 * @author rodche
 */
@Component
public class DnaEntityReferenceRule extends AbstractRule<Dna> {
	@Override
	public boolean canCheck(Object thing) {
		return thing instanceof Dna;
	}

	@Override
	public void check(Dna sm, boolean fix) {
		if(sm.getEntityReference() == null) {
			if(sm.getMemberPhysicalEntity().isEmpty()) {
				error(sm, "null.entity.reference", false, "DnaReference");
			} 
		}
	} 
}
