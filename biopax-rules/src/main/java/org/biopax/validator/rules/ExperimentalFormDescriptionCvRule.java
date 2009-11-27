package org.biopax.validator.rules;

import org.biopax.paxtools.model.level3.ExperimentalFormVocabulary;
import org.biopax.validator.impl.CvTermRestriction;
import org.biopax.validator.impl.CvTermsRule;
import org.biopax.validator.impl.CvTermRestriction.UseChildTerms;
import org.springframework.stereotype.Component;

/**
 * Checks:
 * experimentalForm.experimentalFormDescription: ExperimentalFormVocabulary term.
 *
 * @author rodche
 */
@Component
public class ExperimentalFormDescriptionCvRule extends CvTermsRule<ExperimentalFormVocabulary> {
  
    public ExperimentalFormDescriptionCvRule() {
		super(ExperimentalFormVocabulary.class, null, 
				new CvTermRestriction("MI:0002","MI", false, UseChildTerms.ALL, false),
				new CvTermRestriction("MI:0495","MI", false, UseChildTerms.ALL, false),
				new CvTermRestriction("MI:0346","MI", false, UseChildTerms.ALL, false));
	}
    
}
