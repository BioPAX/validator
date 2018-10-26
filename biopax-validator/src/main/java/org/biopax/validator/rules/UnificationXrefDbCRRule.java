package org.biopax.validator.rules;


import org.biopax.paxtools.model.level3.UnificationXref;
import org.biopax.validator.CardinalityAndRangeRule;
import org.springframework.stereotype.Component;

/**
 * Checks: UnificationXref.db has value
 * 
 * @author rodche
 */
@Component
public class UnificationXrefDbCRRule extends CardinalityAndRangeRule<UnificationXref> {
	public UnificationXrefDbCRRule() {
		super(UnificationXref.class, "db", 1, 1, String.class);
	}
}
