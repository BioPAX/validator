package org.biopax.validator.rules;

import org.biopax.paxtools.model.level3.Score;
import org.biopax.paxtools.model.level3.Provenance;
import org.biopax.validator.impl.Level3CardinalityAndRangeRule;
import org.springframework.stereotype.Component;

/**
 * Checks:
 * Score.scoreSource cardinality/range.
 * @author rodche
 */
@Component
public class ScoreSourceCRRule extends Level3CardinalityAndRangeRule<Score> {
	public ScoreSourceCRRule() {
		super(Score.class, "scoreSource", 0, 1, Provenance.class);
	}
}
