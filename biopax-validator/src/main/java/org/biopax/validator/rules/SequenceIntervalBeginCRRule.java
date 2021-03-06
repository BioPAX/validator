package org.biopax.validator.rules;


import org.biopax.paxtools.model.level3.SequenceInterval;
import org.biopax.paxtools.model.level3.SequenceSite;
import org.biopax.validator.CardinalityAndRangeRule;
import org.springframework.stereotype.Component;

/**
 * Checks:
 * SequenceInterval.sequenceIntervalBegin cardinality/range.
 * @author rodche
 */
@Component
public class SequenceIntervalBeginCRRule extends CardinalityAndRangeRule<SequenceInterval> {
	public SequenceIntervalBeginCRRule() {
		super(SequenceInterval.class, "sequenceIntervalBegin", 0, 1, SequenceSite.class);
	}
}
