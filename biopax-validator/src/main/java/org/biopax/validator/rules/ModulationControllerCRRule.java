package org.biopax.validator.rules;


import org.biopax.paxtools.model.level3.Modulation;
import org.biopax.paxtools.model.level3.Pathway;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.validator.CardinalityAndRangeRule;
import org.springframework.stereotype.Component;

/**
 * Checks: 
 * Modulation.controller cardinality/range 
 * constraint: max. 1 Pathway.class or PhysicalEntity.class
 * 
 * @author rodche
 */
@Component
public class ModulationControllerCRRule extends CardinalityAndRangeRule<Modulation> {
	public ModulationControllerCRRule() {
		super(Modulation.class, "controller", 0, 1, Pathway.class, PhysicalEntity.class);
	}
}
