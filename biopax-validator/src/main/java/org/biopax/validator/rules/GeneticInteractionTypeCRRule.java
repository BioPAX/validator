package org.biopax.validator.rules;

/*
 *
 */

import org.biopax.paxtools.model.level3.GeneticInteraction;
import org.biopax.paxtools.model.level3.InteractionVocabulary;
import org.biopax.validator.impl.CardinalityAndRangeRule;
import org.springframework.stereotype.Component;

/**
 * Checks:
 * GeneticInteraction properties and cardinality constraints.
 * 
 * @author rodche
 */
@Component
public class GeneticInteractionTypeCRRule extends CardinalityAndRangeRule<GeneticInteraction> {
	public GeneticInteractionTypeCRRule() {
		super(GeneticInteraction.class, "interactionType", 0, 1, InteractionVocabulary.class);
	}
}
