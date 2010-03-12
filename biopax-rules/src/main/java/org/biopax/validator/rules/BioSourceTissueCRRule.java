package org.biopax.validator.rules;

import org.biopax.paxtools.model.level3.BioSource;
import org.biopax.paxtools.model.level3.TissueVocabulary;
import org.biopax.validator.impl.Level3CardinalityAndRangeRule;
import org.springframework.stereotype.Component;

/**
 * Checks:
 * BioSource.tissue cardinality/range.
 * @author rodche
 */
@Component
public class BioSourceTissueCRRule extends Level3CardinalityAndRangeRule<BioSource> {
	public BioSourceTissueCRRule() {
		super(BioSource.class, "tissue", 0, 1, TissueVocabulary.class);
	}
}
