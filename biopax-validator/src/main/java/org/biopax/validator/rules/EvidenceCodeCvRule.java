package org.biopax.validator.rules;


import org.biopax.paxtools.model.level3.EvidenceCodeVocabulary;
import org.biopax.validator.api.CvRestriction;
import org.biopax.validator.api.CvRestriction.UseChildTerms;
import org.biopax.validator.CvTermsRule;
import org.springframework.stereotype.Component;

/**
 * Checks:
 * evidence.evidenceCode has valid EvidenceCodeVocabulary term.
 * 
 * (MI does/will include ECO ontology...)
 *
 * @author rodche
 */
@Component
public class EvidenceCodeCvRule extends CvTermsRule<EvidenceCodeVocabulary> {

	public EvidenceCodeCvRule() {
		super(EvidenceCodeVocabulary.class, null, 
				new CvRestriction("MI:0001", "MI", false, UseChildTerms.ALL, false),
				new CvRestriction("MI:0002", "MI", false, UseChildTerms.ALL, false),
				new CvRestriction("MI:0003", "MI", false, UseChildTerms.ALL, false));
	}
    
}
