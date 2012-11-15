package org.biopax.validator.rules;

import org.biopax.paxtools.model.level3.InteractionVocabulary;
import org.biopax.validator.CvRestriction;
import org.biopax.validator.CvRestriction.UseChildTerms;
import org.biopax.validator.impl.Level3CvTermsRule;
import org.springframework.stereotype.Component;


/**
 * Checks:
 * Interaction.interactionType is InteractionTypeVocabulary, with valid term.
 *
 * @author rodche
 */
@Component
public class InteractionTypeCvRule extends Level3CvTermsRule<InteractionVocabulary> {

	public InteractionTypeCvRule() {
		super(InteractionVocabulary.class, null,
				new CvRestriction("MI:0190", "MI", 
						false, UseChildTerms.ALL, false));
	}

}
