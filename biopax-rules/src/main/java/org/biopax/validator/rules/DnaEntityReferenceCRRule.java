package org.biopax.validator.rules;

import org.biopax.paxtools.model.level3.Dna;
import org.biopax.paxtools.model.level3.DnaReference;
import org.biopax.validator.impl.Level3CardinalityAndRangeRule;
import org.springframework.stereotype.Component;

/**
 * Checks:
 * Dna.entityReference cardinality/range.
 * @author rodche
 */
@Component
public class DnaEntityReferenceCRRule extends Level3CardinalityAndRangeRule<Dna> {
	public DnaEntityReferenceCRRule() {
		super(Dna.class, "entityReference", 1, 1, DnaReference.class);
	}
}
