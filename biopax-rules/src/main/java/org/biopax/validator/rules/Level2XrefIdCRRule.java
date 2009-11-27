package org.biopax.validator.rules;

import org.biopax.paxtools.model.level2.xref;
import org.biopax.validator.impl.Level2CardinalityAndRangeRule;
import org.springframework.stereotype.Component;

/**
 * Checks: Xref.id has value (should be warning)
 * 
 * @author rodche
 */
@Component
public class Level2XrefIdCRRule extends Level2CardinalityAndRangeRule<xref> {
	public Level2XrefIdCRRule() {
		super(xref.class, "ID", 0, 1, String.class);
	}
}
