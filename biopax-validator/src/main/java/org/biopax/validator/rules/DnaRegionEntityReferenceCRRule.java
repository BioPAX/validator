package org.biopax.validator.rules;


import org.biopax.paxtools.model.level3.DnaRegion;
import org.biopax.paxtools.model.level3.DnaRegionReference;
import org.biopax.validator.CardinalityAndRangeRule;
import org.springframework.stereotype.Component;

/**
 * Checks:
 * DnaRegion.entityReference cardinality/range.
 * @author rodche
 */
@Component
public class DnaRegionEntityReferenceCRRule extends CardinalityAndRangeRule<DnaRegion> {
	public DnaRegionEntityReferenceCRRule() {
		super(DnaRegion.class, "entityReference", 0, 1, DnaRegionReference.class);
	}
}
