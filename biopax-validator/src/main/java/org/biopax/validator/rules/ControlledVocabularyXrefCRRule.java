package org.biopax.validator.rules;

import org.biopax.paxtools.model.level3.ControlledVocabulary;
import org.biopax.paxtools.model.level3.UnificationXref;
import org.biopax.validator.impl.Level3CardinalityAndRangeRule;
import org.springframework.stereotype.Component;


@Component
public class ControlledVocabularyXrefCRRule extends
		Level3CardinalityAndRangeRule<ControlledVocabulary> {

	public ControlledVocabularyXrefCRRule() {
		super(ControlledVocabulary.class, "xref", 1, 1, UnificationXref.class);
	}
	
}
