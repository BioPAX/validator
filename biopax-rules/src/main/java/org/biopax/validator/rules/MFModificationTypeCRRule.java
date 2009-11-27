package org.biopax.validator.rules;

import org.biopax.paxtools.model.level3.ModificationFeature;
import org.biopax.paxtools.model.level3.SequenceModificationVocabulary;
import org.biopax.validator.impl.CardinalityAndRangeRule;
import org.springframework.stereotype.Component;

/**
 * Checks:
 * ModificationFeature.modificationType cardinality/range.
 * @author rodche
 */
@Component
public class MFModificationTypeCRRule extends CardinalityAndRangeRule<ModificationFeature> {
	public MFModificationTypeCRRule() {
		super(ModificationFeature.class, "modificationType", 1, 1, SequenceModificationVocabulary.class);
	}
}
