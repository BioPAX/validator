package org.biopax.validator.rules;

import org.biopax.paxtools.model.level3.Catalysis;
import org.biopax.paxtools.model.level3.Pathway;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.validator.impl.Level3CardinalityAndRangeRule;
import org.springframework.stereotype.Component;

/**
 * Checks: 
 * Catalysis.controller cardinality/range 
 * constraint: max. 1 Pathway.class or PhysicalEntity.class
 * 
 * @author rodche
 */
@Component
public class CatalysisControllerCRRule extends Level3CardinalityAndRangeRule<Catalysis> {
	public CatalysisControllerCRRule() {
		super(Catalysis.class, "controller", 0, 1, Pathway.class, PhysicalEntity.class);
	}
}
