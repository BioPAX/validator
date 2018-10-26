package org.biopax.validator.rules;


import org.biopax.paxtools.model.level3.RelationshipTypeVocabulary;
import org.biopax.validator.api.CvRestriction;
import org.biopax.validator.api.CvRestriction.UseChildTerms;
import org.biopax.validator.CvTermsRule;
import org.springframework.stereotype.Component;

/**
 * Checks:
 * (RelationshipXref.relationshipType) RelationshipTypeVocabulary terms.
 *
 * @author rodche
 */
@Component
public class RelationshipTypeCvRule extends CvTermsRule<RelationshipTypeVocabulary> {

	public RelationshipTypeCvRule() {
		super(RelationshipTypeVocabulary.class, null,
				new CvRestriction("MI:0353","MI", false,
						UseChildTerms.ALL, false));
	}
	
}
