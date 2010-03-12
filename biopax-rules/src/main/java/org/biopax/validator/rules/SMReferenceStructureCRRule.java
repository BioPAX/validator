package org.biopax.validator.rules;

import org.biopax.paxtools.model.level3.SmallMoleculeReference;
import org.biopax.paxtools.model.level3.ChemicalStructure;
import org.biopax.validator.impl.Level3CardinalityAndRangeRule;
import org.springframework.stereotype.Component;

/**
 * Checks:
 * SmallMoleculeReference.structure cardinality/range.
 * @author rodche
 */
@Component
public class SMReferenceStructureCRRule extends Level3CardinalityAndRangeRule<SmallMoleculeReference> {
	public SMReferenceStructureCRRule() {
		super(SmallMoleculeReference.class, "structure", 0, 1, ChemicalStructure.class);
	}
}
