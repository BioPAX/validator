package org.biopax.validator.rules;

import org.biopax.paxtools.model.level3.Protein;
import org.biopax.paxtools.model.level3.ProteinReference;
import org.biopax.validator.impl.Level3CardinalityAndRangeRule;
import org.springframework.stereotype.Component;

/**
 * Checks:
 * Protein.entityReference functional/range.
 * @author rodche
 */
@Component
public class ProteinEntityReferenceCRRule extends Level3CardinalityAndRangeRule<Protein> {
	public ProteinEntityReferenceCRRule() {
		super(Protein.class, "entityReference", 1, 1, ProteinReference.class);
	}
}
