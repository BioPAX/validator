package org.biopax.validator.rules;

import org.biopax.paxtools.model.level3.BioSource;
import org.biopax.paxtools.model.level3.ProteinReference;
import org.biopax.validator.impl.Level3CardinalityAndRangeRule;
import org.springframework.stereotype.Component;

/**
 * Checks:
 * ProteinReference.organism is a must!
 * @author rodche
 */
@Component
public class ProteinReferenceOrganismCRRule extends Level3CardinalityAndRangeRule<ProteinReference> {
	public ProteinReferenceOrganismCRRule() {
		super(ProteinReference.class, "organism", 1, 1, BioSource.class);
	}
}
