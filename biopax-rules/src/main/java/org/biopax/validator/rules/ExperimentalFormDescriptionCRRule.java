package org.biopax.validator.rules;

import org.biopax.paxtools.model.level3.ExperimentalForm;
import org.biopax.paxtools.model.level3.ExperimentalFormVocabulary;
import org.biopax.validator.impl.Level3CardinalityAndRangeRule;
import org.springframework.stereotype.Component;

/**
 * Checks:
 * ExperimentalForm properties cardinality/range.
 * @author rodche
 */
@Component
public class ExperimentalFormDescriptionCRRule extends Level3CardinalityAndRangeRule<ExperimentalForm> {
    public ExperimentalFormDescriptionCRRule() {
		super(ExperimentalForm.class, "experimentalFormDescription", 1, 0, ExperimentalFormVocabulary.class);
	}
}
