package org.biopax.validator.rules;


import org.biopax.paxtools.model.level3.TissueVocabulary;
import org.biopax.validator.api.CvRestriction;
import org.biopax.validator.api.CvRestriction.UseChildTerms;
import org.biopax.validator.CvTermsRule;
import org.springframework.stereotype.Component;

/**
 * Checks: BioSource.tissue: TissueVocabulary terms.
 * 
 * @author rodche
 */
@Component
public class BioSourceTissueCvRule extends CvTermsRule<TissueVocabulary> {
	public BioSourceTissueCvRule() {
		super(TissueVocabulary.class, null, 
				new CvRestriction("BTO:0000000","BTO", 
						false, UseChildTerms.ALL, false));
	}
}
