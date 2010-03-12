package org.biopax.validator.rules;

import org.biopax.paxtools.model.level3.ControlledVocabulary;
import org.biopax.validator.impl.Level3CardinalityAndRangeRule;
import org.springframework.stereotype.Component;


@Component
public class ControlledVocabularyTermCRRule extends
		Level3CardinalityAndRangeRule<ControlledVocabulary> {

	public ControlledVocabularyTermCRRule() {
		super(ControlledVocabulary.class, "term", 1, 0, String.class);
	}
	
}
