package org.biopax.validator.rules;


import org.biopax.paxtools.model.level3.Gene;
import org.biopax.paxtools.model.level3.BioSource;
import org.biopax.validator.CardinalityAndRangeRule;
import org.springframework.stereotype.Component;

/**
 * Checks:
 * Gene.organism cardinality/range.
 * @author rodche
 */
@Component
public class GeneOrganismCRRule extends CardinalityAndRangeRule<Gene> {
	public GeneOrganismCRRule() {
		super(Gene.class, "organism", 0, 1, BioSource.class);
	}
}
