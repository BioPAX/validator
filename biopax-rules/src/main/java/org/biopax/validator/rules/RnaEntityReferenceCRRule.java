package org.biopax.validator.rules;

import org.biopax.paxtools.model.level3.Rna;
import org.biopax.paxtools.model.level3.RnaReference;
import org.biopax.validator.impl.Level3CardinalityAndRangeRule;
import org.springframework.stereotype.Component;

/**
 * Checks:
 * Rna.entityReference cardinality/range.
 * @author rodche
 */
@Component
public class RnaEntityReferenceCRRule extends Level3CardinalityAndRangeRule<Rna> {
	public RnaEntityReferenceCRRule() {
		super(Rna.class, "entityReference", 1, 1, RnaReference.class);
	}
}
