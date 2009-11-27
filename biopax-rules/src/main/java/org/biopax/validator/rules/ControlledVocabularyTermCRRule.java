package org.biopax.validator.rules;

import org.biopax.paxtools.model.level3.ControlledVocabulary;
import org.biopax.validator.impl.CardinalityAndRangeRule;
import org.springframework.stereotype.Component;

//TODO this is disabled rule (not sure if apply)

//@Component
public class ControlledVocabularyTermCRRule extends
		CardinalityAndRangeRule<ControlledVocabulary> {

	public ControlledVocabularyTermCRRule() {
		super(ControlledVocabulary.class, "term", 1, 0, String.class);
	}
	
}
