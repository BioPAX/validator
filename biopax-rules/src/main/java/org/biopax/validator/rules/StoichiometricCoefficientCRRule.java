package org.biopax.validator.rules;

import org.biopax.paxtools.model.level3.Stoichiometry;
import org.biopax.validator.impl.CardinalityAndRangeRule;
import org.springframework.stereotype.Component;

/**
 * Checks:
 * Stoichiometry.stoichiometricCoefficient
 * @author rodche
 */
@Component
public class StoichiometricCoefficientCRRule extends CardinalityAndRangeRule<Stoichiometry> {
	public StoichiometricCoefficientCRRule() {
		super(Stoichiometry.class, "stoichiometricCoefficient", 1, 1, Float.class);
	}
}
