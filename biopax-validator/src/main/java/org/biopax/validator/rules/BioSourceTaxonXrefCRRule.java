package org.biopax.validator.rules;

/*
 *
 */

import org.biopax.paxtools.model.level3.BioSource;
import org.biopax.paxtools.model.level3.UnificationXref;
import org.biopax.validator.impl.CardinalityAndRangeRule;
import org.springframework.stereotype.Component;

/**
 * Checks:
 * BioSource.taxonXref cardinality/range.
 * @author rodche
 */
@Component
public class BioSourceTaxonXrefCRRule extends CardinalityAndRangeRule<BioSource> {
	public BioSourceTaxonXrefCRRule() {
		super(BioSource.class, "xref", 0, 1, UnificationXref.class);
	}
}
