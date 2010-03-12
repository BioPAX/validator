package org.biopax.validator.rules;

import org.biopax.paxtools.model.level3.SequenceRegionVocabulary;
import org.biopax.validator.impl.CvTermRestriction;
import org.biopax.validator.impl.Level3CvTermsRule;
import org.biopax.validator.impl.CvTermRestriction.UseChildTerms;
import org.springframework.stereotype.Component;

/**
 * Checks SequenceRegionVocabulary terms against the recommended ontology,
 * not taking into account whether this controlled vocabulary is
 * the value of 'regionType' or 'featureLocationType' property.
 * 
 * @author rodch
 *
 */

@Component
public class SequenceRegionCvRule extends Level3CvTermsRule<SequenceRegionVocabulary> {

	public SequenceRegionCvRule() {
		super(SequenceRegionVocabulary.class, null, 
				new CvTermRestriction("SO:0000001","SO", false, 
						UseChildTerms.DIRECT, false)); // OntologyManager bug: this fails to init when using 'ALL'!
	}
	
}
