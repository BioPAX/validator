package org.biopax.validator.rules;


import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.model.level3.CellularLocationVocabulary;
import org.biopax.validator.CardinalityAndRangeRule;
import org.springframework.stereotype.Component;

/**
 * Checks:
 * PhysicalEntity.cellularLocation cardinality/range.
 * @author rodche
 */
@Component
public class PhysicalEntityCellularLocationCRRule extends CardinalityAndRangeRule<PhysicalEntity> {
	public PhysicalEntityCellularLocationCRRule() {
		super(PhysicalEntity.class, "cellularLocation", 0, 1, CellularLocationVocabulary.class);
	}
}
