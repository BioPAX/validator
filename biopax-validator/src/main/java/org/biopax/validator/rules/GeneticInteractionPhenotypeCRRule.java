package org.biopax.validator.rules;

/*
 *
 */

import org.biopax.paxtools.model.level3.GeneticInteraction;
import org.biopax.paxtools.model.level3.PhenotypeVocabulary;
import org.biopax.validator.impl.CardinalityAndRangeRule;
import org.springframework.stereotype.Component;

/**
 * Checks:
 * GeneticInteraction properties and cardinality constraints.
 * 
 * @author rodche
 */
@Component
public class GeneticInteractionPhenotypeCRRule extends CardinalityAndRangeRule<GeneticInteraction> {
	public GeneticInteractionPhenotypeCRRule() {
		super(GeneticInteraction.class, "phenotype", 1, 1, PhenotypeVocabulary.class);
	}
}
