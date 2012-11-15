package org.biopax.validator.rules;

import org.biopax.paxtools.model.level3.CellVocabulary;
import org.biopax.validator.CvRestriction;
import org.biopax.validator.CvRestriction.UseChildTerms;
import org.biopax.validator.impl.Level3CvTermsRule;
import org.springframework.stereotype.Component;

/**
 * Checks:
 * (BioSource) cellType (CellVocabulary) - CL:0000000 children terms.
 *
 * @author rodche
 */
@Component
public class BioSourceCellTypeCvRule extends Level3CvTermsRule<CellVocabulary> {

	public BioSourceCellTypeCvRule() {
		super(CellVocabulary.class, null, 
				new CvRestriction("CL:0000000","CL",false,UseChildTerms.ALL,false));
	}
	
}
