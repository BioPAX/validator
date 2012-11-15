package org.biopax.validator.rules;

import org.biopax.paxtools.model.level3.ConversionDirectionType;
import org.biopax.paxtools.model.level3.Degradation;
import org.biopax.validator.result.Validation;
import org.biopax.validator.impl.AbstractRule;
import org.springframework.stereotype.Component;

/**
 * Checks Degradation.conversionDirection value only "LEFT_TO_RIGHT"
 * 
 * @author rodche
 */
@Component
public class DegradationConversionDirectionRule extends AbstractRule<Degradation> {

	public boolean canCheck(Object thing) {
		return thing instanceof Degradation;
	}

	public void check(final Validation validation, Degradation thing) {
		if(thing.getConversionDirection() != null 
			&& thing.getConversionDirection() != ConversionDirectionType.LEFT_TO_RIGHT) 
		{	
			error(validation, thing, "range.violated", 
						validation.isFix(), "conversionDirection",
					thing.getConversionDirection().name(), "", ConversionDirectionType.LEFT_TO_RIGHT.name()
					+ " (or empty)");
			if(validation.isFix()) {
				thing.setConversionDirection(ConversionDirectionType.LEFT_TO_RIGHT);
			}
		}
		
	}

}
