package org.biopax.validator.rules;

import org.biopax.paxtools.model.level3.Xref;
import org.biopax.validator.impl.CardinalityAndRangeRule;
import org.springframework.stereotype.Component;

/**
 * Checks: Xref.db has value (should be warning)
 * 
 * @author rodche
 */
@Component
public class XrefDbCRRule extends CardinalityAndRangeRule<Xref> {
	public XrefDbCRRule() {
		super(Xref.class, "db", 1, 1, String.class);
	}
}
