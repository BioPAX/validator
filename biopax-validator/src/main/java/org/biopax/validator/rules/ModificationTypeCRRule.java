package org.biopax.validator.rules;


import org.biopax.paxtools.model.level3.ModificationFeature;
import org.biopax.paxtools.model.level3.SequenceModificationVocabulary;
import org.biopax.validator.CardinalityAndRangeRule;
import org.springframework.stereotype.Component;

/**
 * Checks:
 * ModificationFeature.modificationType cardinality/range.
 * @author rodche
 */
@Component
public class ModificationTypeCRRule extends CardinalityAndRangeRule<ModificationFeature> {
	public ModificationTypeCRRule() {
		super(ModificationFeature.class, "modificationType", 0, 1, SequenceModificationVocabulary.class);
	}
}
