package org.biopax.validator.rules;

import org.biopax.paxtools.model.level3.EntityReference;
import org.biopax.paxtools.model.level3.EntityReferenceTypeVocabulary;
import org.biopax.validator.impl.CardinalityAndRangeRule;
import org.springframework.stereotype.Component;

/**
 * Checks:
 * EntityReference properties cardinality/range.
 * @author rodche
 */
@Component
public class EntityReferenceTypeCRRule extends CardinalityAndRangeRule<EntityReference> {
	public EntityReferenceTypeCRRule() {
		super(EntityReference.class, "entityReferenceType", 0, 1, EntityReferenceTypeVocabulary.class);
	}
}
