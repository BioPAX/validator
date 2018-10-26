package org.biopax.validator.rules;


import org.biopax.paxtools.model.level3.RelationshipXref;
import org.biopax.validator.CardinalityAndRangeRule;
import org.springframework.stereotype.Component;

/**
 * Checks: RelationshipXref.id has value (should be warning)
 * 
 * @author rodche
 */
@Component
public class XrefIdCRRule extends CardinalityAndRangeRule<RelationshipXref> {
	public XrefIdCRRule() {
		super(RelationshipXref.class, "id", 1, 1, String.class);
	}
}
