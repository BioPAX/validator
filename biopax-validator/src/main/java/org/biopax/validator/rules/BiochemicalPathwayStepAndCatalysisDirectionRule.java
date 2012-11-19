package org.biopax.validator.rules;

import org.biopax.paxtools.model.level3.BiochemicalPathwayStep;
import org.biopax.paxtools.model.level3.Catalysis;
import org.biopax.paxtools.model.level3.CatalysisDirectionType;
import org.biopax.paxtools.model.level3.Conversion;
import org.biopax.paxtools.model.level3.ConversionDirectionType;
import org.biopax.paxtools.model.level3.Process;
import org.biopax.paxtools.model.level3.StepDirection;
import org.biopax.validator.api.AbstractRule;
import org.biopax.validator.api.beans.Validation;
import org.springframework.stereotype.Component;

/**
 * Checks:
 * BiochemicalPathwayStep.stepDirection must be set (otherwise, 
 * consider using PathwayStep instead), and step process's (if any) 
 * catalysisDirection must be the same or empty value, and step conversion's 
 * conversionDirection - the same or REVERSIBLE. 
 * 
 * @author rodche
 */
@Component
public class BiochemicalPathwayStepAndCatalysisDirectionRule extends AbstractRule<BiochemicalPathwayStep> {

	private void fix(BiochemicalPathwayStep t, Object... values) 
	{	
		if(values[0] instanceof Catalysis) {
			((Catalysis)values[0]).setCatalysisDirection((CatalysisDirectionType) values[1]);
		} else if (values[0] instanceof Conversion){
			((Conversion)values[0]).setConversionDirection((ConversionDirectionType) values[1]);
		}
		
	}

	public boolean canCheck(Object thing) {
		return thing instanceof BiochemicalPathwayStep;
	}

	public void check(final Validation validation, BiochemicalPathwayStep step) {
		if(step.getStepDirection() != null) 
		{
			final CatalysisDirectionType correctDir = (step.getStepDirection() == StepDirection.LEFT_TO_RIGHT) 
					? CatalysisDirectionType.LEFT_TO_RIGHT
						: CatalysisDirectionType.RIGHT_TO_LEFT;
			
			for(Process proc : step.getStepProcess() ) {
				if(proc instanceof Catalysis) 
				{
					CatalysisDirectionType cdir = ((Catalysis) proc).getCatalysisDirection();
					if(cdir != null && cdir != correctDir) {
						error(validation, step, "direction.conflict", 
								validation.isFix(), "stepDirection=" + step.getStepDirection(), 
						proc, "catalysisDirection=" + cdir);
						if(validation.isFix()) {
							fix(step, proc, null);
						}
					}
				}
			}
			
			Conversion con = step.getStepConversion();
			if( con != null 
				&& con.getConversionDirection() != null
				&& con.getConversionDirection() != ConversionDirectionType.REVERSIBLE) 
			{
				error(validation, step, "direction.conflict", 
						validation.isFix(), "stepDirection=" + step.getStepDirection(), con, "conversionDirection=" 
						+ con.getConversionDirection() + ", must be REVERSIBLE or empty");
				if(validation.isFix()) {
					fix(step, con, null);
				}
			}
		} else {
			error(validation, step, "direction.conflict", false, "'stepDirection' is null");
		}
		
	}
	
}
