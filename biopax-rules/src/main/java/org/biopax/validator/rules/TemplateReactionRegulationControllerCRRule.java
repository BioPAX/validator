package org.biopax.validator.rules;

import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.model.level3.TemplateReactionRegulation;
import org.biopax.validator.impl.Level3CardinalityAndRangeRule;
import org.springframework.stereotype.Component;

/**
 * Checks: 
 * TemplateReactionRegulation.controller cardinality range 
 * constraint: none or many PhysicalEntity
 * 
 * @author rodche
 */
@Component
public class TemplateReactionRegulationControllerCRRule extends Level3CardinalityAndRangeRule<TemplateReactionRegulation> {
	public TemplateReactionRegulationControllerCRRule() {
		super(TemplateReactionRegulation.class, "controller", 0, 0, PhysicalEntity.class);
	}
}
