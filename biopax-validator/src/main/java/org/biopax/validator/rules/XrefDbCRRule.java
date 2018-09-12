package org.biopax.validator.rules;

/*
 *
 */

import org.biopax.paxtools.model.level3.RelationshipXref;
import org.biopax.validator.impl.CardinalityAndRangeRule;
import org.springframework.stereotype.Component;

/**
 * Checks: RelationshipXref.db has value (should be warning)
 * 
 * @author rodche
 */
@Component
public class XrefDbCRRule extends CardinalityAndRangeRule<RelationshipXref> {
	public XrefDbCRRule() {
		super(RelationshipXref.class, "db", 1, 1, String.class);
	}
}
