package org.biopax.validator.rules;


import org.biopax.paxtools.model.level3.SmallMoleculeReference;
import org.biopax.paxtools.model.level3.ChemicalStructure;
import org.biopax.validator.CardinalityAndRangeRule;
import org.springframework.stereotype.Component;

/**
 * Checks:
 * SmallMoleculeReference.structure cardinality/range.
 * @author rodche
 */
@Component
public class SmrStructureCRRule extends CardinalityAndRangeRule<SmallMoleculeReference> {
	public SmrStructureCRRule() {
		super(SmallMoleculeReference.class, "structure", 0, 1, ChemicalStructure.class);
	}
}
