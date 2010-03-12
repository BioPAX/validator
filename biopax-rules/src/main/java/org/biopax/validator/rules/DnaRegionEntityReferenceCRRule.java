package org.biopax.validator.rules;

import org.biopax.paxtools.model.level3.DnaRegion;
import org.biopax.paxtools.model.level3.DnaRegionReference;
import org.biopax.validator.impl.Level3CardinalityAndRangeRule;
import org.springframework.stereotype.Component;

/**
 * Checks:
 * DnaRegion.entityReference cardinality/range.
 * @author rodche
 */
@Component
public class DnaRegionEntityReferenceCRRule extends Level3CardinalityAndRangeRule<DnaRegion> {
	public DnaRegionEntityReferenceCRRule() {
		super(DnaRegion.class, "entityReference", 1, 1, DnaRegionReference.class);
	}
}
