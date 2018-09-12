package org.biopax.validator.rules;

/*
 *
 */

import org.biopax.paxtools.model.level3.UnificationXref;
import org.biopax.validator.impl.CardinalityAndRangeRule;
import org.springframework.stereotype.Component;

/**
 * Checks: UnificationXref.id has value
 * 
 * @author rodche
 */
@Component
public class UnificationXrefIdCRRule extends CardinalityAndRangeRule<UnificationXref> {
	public UnificationXrefIdCRRule() {
		super(UnificationXref.class, "id", 1, 1, String.class);
	}
}
