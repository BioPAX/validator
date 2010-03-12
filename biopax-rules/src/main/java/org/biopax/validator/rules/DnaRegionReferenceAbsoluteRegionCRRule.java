package org.biopax.validator.rules;

import org.biopax.paxtools.model.level3.DnaRegionReference;
import org.biopax.paxtools.model.level3.SequenceLocation;
import org.biopax.validator.impl.Level3CardinalityAndRangeRule;
import org.springframework.stereotype.Component;

/**
 * Checks:
 * DnaRegionReference properties cardinality/range.
 * @author rodche
 */
@Component
public class DnaRegionReferenceAbsoluteRegionCRRule extends Level3CardinalityAndRangeRule<DnaRegionReference> {

	public DnaRegionReferenceAbsoluteRegionCRRule() {
		super(DnaRegionReference.class, "absoluteRegion", 0, 1, SequenceLocation.class);
	}    
}
