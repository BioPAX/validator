package org.biopax.validator.rules;

import org.biopax.paxtools.model.level3.Catalysis;
import org.biopax.paxtools.model.level3.CatalysisDirectionType;
import org.biopax.validator.impl.CardinalityAndRangeRule;
import org.springframework.stereotype.Component;

/**
 * Checks: 
 * Catalysis.catalysisDirection cardinality/range 
 * constraint: none or one CatalysisDirectionType
 * 
 * @author rodche
 */
@Component
public class CatalysisDirectionCRRule extends CardinalityAndRangeRule<Catalysis> {
	public CatalysisDirectionCRRule() {
		super(Catalysis.class, "catalysisDirection", 0, 1, CatalysisDirectionType.class);
	}
}
