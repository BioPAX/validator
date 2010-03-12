package org.biopax.validator.rules;

import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.validator.impl.Level3CardinalityAndRangeRule;
import org.springframework.stereotype.Component;

/**
 * Checks PhysicalEntity.displayName is present.
 * @author rodche
 */
@Component
public class PhysicalEntityDisplayNameCRRule extends Level3CardinalityAndRangeRule<PhysicalEntity> {
	public PhysicalEntityDisplayNameCRRule() {
		super(PhysicalEntity.class, "displayName", 1, 1, String.class);
	}
}
