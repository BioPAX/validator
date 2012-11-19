package org.biopax.validator.rules;

import org.biopax.paxtools.model.level3.RnaRegion;
import org.biopax.paxtools.model.level3.RnaRegionReference;
import org.biopax.validator.impl.CardinalityAndRangeRule;
import org.springframework.stereotype.Component;

/**
 * Checks:
 * RnaRegion.entityReference cardinality/range.
 * @author rodche
 */
@Component
public class RnaRegionEntityReferenceCRRule extends CardinalityAndRangeRule<RnaRegion> {
	public RnaRegionEntityReferenceCRRule() {
		super(RnaRegion.class, "entityReference", 0, 1, RnaRegionReference.class);
	}
}
