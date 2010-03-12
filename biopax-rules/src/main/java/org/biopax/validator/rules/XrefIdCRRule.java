package org.biopax.validator.rules;

import org.biopax.paxtools.model.level3.Xref;
import org.biopax.validator.impl.Level3CardinalityAndRangeRule;
import org.springframework.stereotype.Component;

/**
 * Checks: Xref.id has value (should be warning)
 * 
 * @author rodche
 */
@Component
public class XrefIdCRRule extends Level3CardinalityAndRangeRule<Xref> {
	public XrefIdCRRule() {
		super(Xref.class, "id", 0, 1, String.class);
	}
}
