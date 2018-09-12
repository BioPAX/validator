package org.biopax.validator.rules;

/*
 *
 */

import org.biopax.paxtools.model.level3.Pathway;
import org.biopax.paxtools.model.level3.BioSource;
import org.biopax.validator.impl.CardinalityAndRangeRule;
import org.springframework.stereotype.Component;

/**
 * Checks:
 * Pathway.organism cardinality/range.
 * @author rodche
 */
@Component
public class PathwayOrganismCRRule extends CardinalityAndRangeRule<Pathway> {
	public PathwayOrganismCRRule() {
		super(Pathway.class, "organism", 0, 1, BioSource.class);
	}
}
