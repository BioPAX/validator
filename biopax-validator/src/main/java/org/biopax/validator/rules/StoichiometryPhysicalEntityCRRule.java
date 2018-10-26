package org.biopax.validator.rules;


import org.biopax.paxtools.model.level3.Stoichiometry;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.validator.CardinalityAndRangeRule;
import org.springframework.stereotype.Component;

/**
 * Checks:
 * Stoichiometry.physicalEntity cardinality/range.
 * @author rodche
 */
@Component
public class StoichiometryPhysicalEntityCRRule extends CardinalityAndRangeRule<Stoichiometry> {
	public StoichiometryPhysicalEntityCRRule() {
		super(Stoichiometry.class, "physicalEntity", 1, 1, PhysicalEntity.class);
	}
}
