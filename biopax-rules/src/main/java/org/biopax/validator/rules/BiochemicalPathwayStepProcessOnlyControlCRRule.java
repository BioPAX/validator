package org.biopax.validator.rules;

import org.biopax.paxtools.model.level3.BiochemicalPathwayStep;
import org.biopax.paxtools.model.level3.Control;
import org.biopax.validator.impl.Level3CardinalityAndRangeRule;
import org.springframework.stereotype.Component;

/**
 * Checks:
 * BiochemicalPathwayStep.stepProcess range is Control.
 */
@Component
public class BiochemicalPathwayStepProcessOnlyControlCRRule 
	extends Level3CardinalityAndRangeRule<BiochemicalPathwayStep> 
{
	public BiochemicalPathwayStepProcessOnlyControlCRRule() {
		super(BiochemicalPathwayStep.class, "stepProcess", 0, 0, Control.class);
	}    
}
