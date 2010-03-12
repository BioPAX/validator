package org.biopax.validator.rules;

import org.biopax.paxtools.model.level3.RnaRegion;
import org.biopax.paxtools.model.level3.RnaRegionReference;
import org.biopax.validator.impl.Level3CardinalityAndRangeRule;
import org.springframework.stereotype.Component;

/**
 * Checks:
 * RnaRegion.entityReference cardinality/range.
 * @author rodche
 */
@Component
public class RnaRegionEntityReferenceCRRule extends Level3CardinalityAndRangeRule<RnaRegion> {
	public RnaRegionEntityReferenceCRRule() {
		super(RnaRegion.class, "entityReference", 1, 1, RnaRegionReference.class);
	}
}
