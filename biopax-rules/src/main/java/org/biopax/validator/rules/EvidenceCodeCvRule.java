package org.biopax.validator.rules;

import org.biopax.paxtools.model.level3.EvidenceCodeVocabulary;
import org.biopax.validator.impl.CvTermRestriction;
import org.biopax.validator.impl.Level3CvTermsRule;
import org.biopax.validator.impl.CvTermRestriction.UseChildTerms;
import org.springframework.stereotype.Component;

/**
 * Checks:
 * evidence.evidenceCode has valid EvidenceCodeVocabulary term.
 *
 * @author rodche
 */
@Component
public class EvidenceCodeCvRule extends Level3CvTermsRule<EvidenceCodeVocabulary> {

	public EvidenceCodeCvRule() {
		super(EvidenceCodeVocabulary.class, null, 
				new CvTermRestriction("MI:0001", "MI", false, UseChildTerms.ALL, false),
				new CvTermRestriction("MI:0002", "MI", false, UseChildTerms.ALL, false),
				new CvTermRestriction("MI:0003", "MI", false, UseChildTerms.ALL, false));
	}
    
}
