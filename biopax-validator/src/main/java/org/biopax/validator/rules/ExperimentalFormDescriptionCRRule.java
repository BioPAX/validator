package org.biopax.validator.rules;

import org.biopax.paxtools.model.level3.ExperimentalForm;
import org.biopax.paxtools.model.level3.ExperimentalFormVocabulary;
import org.biopax.validator.impl.CardinalityAndRangeRule;
import org.springframework.stereotype.Component;

/**
 * Checks:
 * ExperimentalForm properties cardinality/range: 
 * 	experimentalFormDescription must have at least one ExperimentalFormVocabulary value.
 * @author rodche
 */
@Component
public class ExperimentalFormDescriptionCRRule extends CardinalityAndRangeRule<ExperimentalForm> {
    public ExperimentalFormDescriptionCRRule() {
		super(ExperimentalForm.class, "experimentalFormDescription", 1, 0, ExperimentalFormVocabulary.class);
	}
}
