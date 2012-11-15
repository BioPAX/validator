package org.biopax.validator.rules;

import org.biopax.paxtools.model.level3.TissueVocabulary;
import org.biopax.validator.CvRestriction;
import org.biopax.validator.CvRestriction.UseChildTerms;
import org.biopax.validator.impl.Level3CvTermsRule;
import org.springframework.stereotype.Component;

/**
 * Checks: BioSource.tissue: TissueVocabulary terms.
 * 
 * @author rodche
 */
@Component
public class BioSourceTissueCvRule extends Level3CvTermsRule<TissueVocabulary> {
	public BioSourceTissueCvRule() {
		super(TissueVocabulary.class, null, 
				new CvRestriction("BTO:0000000","BTO", 
						false, UseChildTerms.ALL, false));
	}
}
