package org.biopax.validator.rules;

import org.biopax.paxtools.model.level3.BiochemicalPathwayStep;
import org.biopax.paxtools.model.level3.Catalysis;
import org.biopax.paxtools.model.level3.CatalysisDirectionType;
import org.biopax.paxtools.model.level3.PathwayStep;
import org.biopax.paxtools.model.level3.StepDirection;
import org.biopax.validator.impl.AbstractRule;
import org.springframework.stereotype.Component;

/**
 * Checks:
 * If direction of the Catalysis instance contained in the step is "LEFT-TO-RIGHT", 
 * then stepDirection of BiochemicalPathwayStep is blank (unknown, unspecified) or LEFT-TO-RIGHT
 * @author rodche
 */
@Component
public class CatalysisAndStepDirectionRule extends AbstractRule<Catalysis> {

	@Override
	public void fix(Catalysis t, Object... values) {	
	}

	public boolean canCheck(Object thing) {
		return thing instanceof Catalysis;
	}

	public void check(Catalysis thing) {
		if(thing != null && 
				thing.getCatalysisDirection() == CatalysisDirectionType.LEFT_TO_RIGHT) 
		{
			for(PathwayStep ps : thing.getStepProcessOf()) {
				if (ps instanceof BiochemicalPathwayStep) {
					StepDirection sdir = ((BiochemicalPathwayStep) ps).getStepDirection();
					if(sdir != null && sdir != StepDirection.LEFT_TO_RIGHT) {
						error(thing, "direction.conflict", 
								"catalysisDirection=" + thing.getCatalysisDirection(), 
								ps, "stepDirection=" + sdir);
					}
				} 
			}
		}
		
	}
	
}
