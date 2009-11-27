package org.biopax.validator.rules;

import org.biopax.paxtools.model.level3.EntityReference;
import org.biopax.validator.impl.CvTermRestriction;
import org.biopax.validator.impl.CvTermsRule;
import org.biopax.validator.impl.CvTermRestriction.UseChildTerms;
import org.springframework.stereotype.Component;

/**
 * Checks:
 * EntityReference.entityReferenceType is a EntityReferenceTypeVocabulary and has valid terms.
 * A controlled vocabulary term that is used to describe the type of grouping such as homology or functional group.
 * 
 * TODO now this rule is disabled as there are no recommended terms; this rule can be different for different ER types.
 *
 * @author rodche
 */
//@Component
public class EntityReferenceTypeCvRule extends CvTermsRule<EntityReference> {

	public EntityReferenceTypeCvRule() {
		super(EntityReference.class, "entityReferenceType", 
				new CvTermRestriction("","", false, 
						UseChildTerms.ALL, false));
	}

	@Override
	public boolean canCheck(Object thing) {
		return super.canCheck(thing) &&
		 ((EntityReference)thing).getEntityReferenceType() != null
		 && !((EntityReference)thing).getEntityReferenceType().isEmpty();
	}
}
