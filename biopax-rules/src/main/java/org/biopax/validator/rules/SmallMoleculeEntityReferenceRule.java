package org.biopax.validator.rules;

import org.biopax.paxtools.model.level3.SmallMolecule;
import org.biopax.validator.impl.AbstractRule;
import org.springframework.stereotype.Component;

/**
 * Checks:
 * SmallMolecule.entityReference is not null when its memberPhysicalEntity is null.
 * @author rodche
 */
@Component
public class SmallMoleculeEntityReferenceRule extends AbstractRule<SmallMolecule> {
	@Override
	public boolean canCheck(Object thing) {
		return thing instanceof SmallMolecule;
	}

	@Override
	public void check(SmallMolecule sm, boolean fix) {
		if(sm.getEntityReference() == null) {
			if(sm.getMemberPhysicalEntity().isEmpty()) {
				error(sm, "null.entity.reference", false, "SmallMoleculeReference");
			} 
		}
	} 
}
