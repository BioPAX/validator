package org.biopax.validator.rules;

import org.biopax.paxtools.model.level3.RnaRegionReference;
import org.biopax.paxtools.model.level3.SequenceLocation;
import org.biopax.validator.impl.Level3CardinalityAndRangeRule;
import org.springframework.stereotype.Component;

/**
 * Checks:
 * RnaRegionReference properties cardinality/range.
 * @author rodche
 */
@Component
public class RnaRegionReferenceAbsoluteRegionCRRule extends Level3CardinalityAndRangeRule<RnaRegionReference> {

	public RnaRegionReferenceAbsoluteRegionCRRule() {
		super(RnaRegionReference.class, "absoluteRegion", 0, 1, SequenceLocation.class);
	}    
}
