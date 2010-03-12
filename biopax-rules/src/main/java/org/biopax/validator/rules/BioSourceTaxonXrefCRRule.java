package org.biopax.validator.rules;

import org.biopax.paxtools.model.level3.BioSource;
import org.biopax.paxtools.model.level3.UnificationXref;
import org.biopax.validator.impl.Level3CardinalityAndRangeRule;
import org.springframework.stereotype.Component;

/**
 * Checks:
 * BioSource.taxonXref cardinality/range.
 * @author rodche
 */
@Component
public class BioSourceTaxonXrefCRRule extends Level3CardinalityAndRangeRule<BioSource> {
	public BioSourceTaxonXrefCRRule() {
		super(BioSource.class, "taxonXref", 0, 1, UnificationXref.class);
	}
}
