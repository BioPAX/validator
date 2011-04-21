package org.biopax.validator.rules;

import org.biopax.paxtools.model.level3.BiochemicalPathwayStep;
import org.biopax.paxtools.model.level3.Catalysis;
import org.biopax.paxtools.model.level3.CatalysisDirectionType;
import org.biopax.paxtools.model.level3.Conversion;
import org.biopax.paxtools.model.level3.ConversionDirectionType;
import org.biopax.paxtools.model.level3.Process;
import org.biopax.validator.impl.AbstractRule;
import org.springframework.stereotype.Component;

/**
 * Checks:
 * If stepDirection of BiochemicalPathwayStep is not empty, then direction 
 * of the Catalysis instance is either blank or "LEFT-TO-RIGHT";
 * and the corresponding conversionDirection property (of the Conversion, if any) 
 * in the stepConversion property is specified as "REVERSIBLE" (or empty).
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

	public void check(BiochemicalPathwayStep step, boolean fix) {
		if(step != null && step.getStepDirection() != null) 
		{
			for(Process proc : step.getStepProcess() ) {
				if(proc instanceof Catalysis) 
				{
					CatalysisDirectionType cdir = ((Catalysis) proc).getCatalysisDirection();
					if(cdir != null && cdir != CatalysisDirectionType.LEFT_TO_RIGHT) {
						error(step, "direction.conflict", fix, 
							"stepDirection=" + step.getStepDirection(), proc, 
							"catalysisDirection=" + cdir + ", must be LEFT_TO_RIGHT");
						if(fix) {
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
				error(step, "direction.conflict", fix, 
					"stepDirection=" + step.getStepDirection(), con, "conversionDirection=" 
						+ con.getConversionDirection() + ", must be REVERSIBLE or empty");
				if(fix) {
					fix(step, con, ConversionDirectionType.REVERSIBLE);
				}
			}
		}
		
	}
	
}
