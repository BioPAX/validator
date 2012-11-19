package org.biopax.validator.rules;

import org.biopax.paxtools.model.level3.Gene;
import org.biopax.paxtools.model.level3.GeneticInteraction;
import org.biopax.validator.impl.CardinalityAndRangeRule;
import org.springframework.stereotype.Component;

/**
 * Checks:
 * GeneticInteraction properties and cardinality constraints.
 * 
 * @author rodche
 */
@Component
public class GeneticInteractionParticipantCRRule extends CardinalityAndRangeRule<GeneticInteraction> {
	public GeneticInteractionParticipantCRRule() {
		super(GeneticInteraction.class, "participant", 2, 0, Gene.class);
	}
}
