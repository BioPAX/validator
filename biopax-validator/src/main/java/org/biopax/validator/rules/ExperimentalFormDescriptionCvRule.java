package org.biopax.validator.rules;


import org.biopax.paxtools.model.level3.ExperimentalFormVocabulary;
import org.biopax.validator.api.CvRestriction;
import org.biopax.validator.api.CvRestriction.UseChildTerms;
import org.biopax.validator.CvTermsRule;
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
				new CvRestriction("MI:0002","MI", false, UseChildTerms.ALL, false),
				new CvRestriction("MI:0495","MI", false, UseChildTerms.ALL, false),
				new CvRestriction("MI:0346","MI", false, UseChildTerms.ALL, false));
	}
    
}
