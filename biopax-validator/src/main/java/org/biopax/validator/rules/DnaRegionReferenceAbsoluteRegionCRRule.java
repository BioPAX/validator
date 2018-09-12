package org.biopax.validator.rules;

/*
 *
 */

import org.biopax.paxtools.model.level3.DnaRegionReference;
import org.biopax.paxtools.model.level3.SequenceLocation;
import org.biopax.validator.impl.CardinalityAndRangeRule;
import org.springframework.stereotype.Component;

/**
 * Checks:
 * DnaRegionReference absoluteRegion cardinality=0,1 (Functional), range = SequenceLocation.
 * @author rodche
 */
@Component
public class DnaRegionReferenceAbsoluteRegionCRRule extends CardinalityAndRangeRule<DnaRegionReference> {

	public DnaRegionReferenceAbsoluteRegionCRRule() {
		super(DnaRegionReference.class, "absoluteRegion", 0, 1, SequenceLocation.class);
	}    
}
