package org.biopax.validator.rules;


import org.biopax.paxtools.model.level3.SmallMolecule;
import org.biopax.paxtools.model.level3.SmallMoleculeReference;
import org.biopax.validator.CardinalityAndRangeRule;
import org.springframework.stereotype.Component;

/**
 * Checks:
 * SmallMolecule.entityReference cardinality/range.
 * @author rodche
 */
@Component
public class SmallMoleculeEntityReferenceCRRule extends CardinalityAndRangeRule<SmallMolecule> {
	public SmallMoleculeEntityReferenceCRRule() {
		super(SmallMolecule.class, "entityReference", 0, 1, SmallMoleculeReference.class);
	}
}
