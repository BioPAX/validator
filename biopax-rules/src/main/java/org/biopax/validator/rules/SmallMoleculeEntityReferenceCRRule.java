package org.biopax.validator.rules;

import org.biopax.paxtools.model.level3.SmallMolecule;
import org.biopax.paxtools.model.level3.SmallMoleculeReference;
import org.biopax.validator.impl.Level3CardinalityAndRangeRule;
import org.springframework.stereotype.Component;

/**
 * Checks:
 * SmallMolecule.entityReference cardinality/range.
 * @author rodche
 */
@Component
public class SmallMoleculeEntityReferenceCRRule extends Level3CardinalityAndRangeRule<SmallMolecule> {
	public SmallMoleculeEntityReferenceCRRule() {
		super(SmallMolecule.class, "entityReference", 1, 1, SmallMoleculeReference.class);
	}
}
