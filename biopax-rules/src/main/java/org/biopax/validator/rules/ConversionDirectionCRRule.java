package org.biopax.validator.rules;

import org.biopax.paxtools.model.level3.Conversion;
import org.biopax.paxtools.model.level3.ConversionDirectionType;
import org.biopax.validator.impl.CardinalityAndRangeRule;
import org.springframework.stereotype.Component;

/**
 * Checks Conversion.conversionDirection cardinality/range: 0 or 1 ConversionDirectionType value
 * 
 * @author rodche
 */
@Component
public class ConversionDirectionCRRule extends CardinalityAndRangeRule<Conversion> {
	public ConversionDirectionCRRule() {
		super(Conversion.class, "conversionDirection", 0, 1, ConversionDirectionType.class);
	}
}
