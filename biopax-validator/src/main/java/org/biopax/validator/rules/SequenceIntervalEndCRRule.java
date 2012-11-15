package org.biopax.validator.rules;

import org.biopax.paxtools.model.level3.SequenceInterval;
import org.biopax.paxtools.model.level3.SequenceSite;
import org.biopax.validator.impl.Level3CardinalityAndRangeRule;
import org.springframework.stereotype.Component;

/**
 * Checks:
 * SequenceInterval.sequenceIntervalEnd cardinality/range.
 * @author rodche
 */
@Component
public class SequenceIntervalEndCRRule extends Level3CardinalityAndRangeRule<SequenceInterval> {
	public SequenceIntervalEndCRRule() {
		super(SequenceInterval.class, "sequenceIntervalEnd", 0, 1, SequenceSite.class);
	}
}
