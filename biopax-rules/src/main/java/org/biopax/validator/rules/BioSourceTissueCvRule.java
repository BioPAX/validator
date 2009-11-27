package org.biopax.validator.rules;

import org.biopax.paxtools.model.level3.TissueVocabulary;
import org.biopax.validator.impl.CvTermRestriction;
import org.biopax.validator.impl.CvTermsRule;
import org.biopax.validator.impl.CvTermRestriction.UseChildTerms;
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
				new CvTermRestriction("BTO:0000000","BTO", 
						false, UseChildTerms.ALL, false));
	}
}
