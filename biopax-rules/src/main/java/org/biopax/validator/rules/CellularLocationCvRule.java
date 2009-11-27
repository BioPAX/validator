package org.biopax.validator.rules;

import org.biopax.paxtools.model.level3.CellularLocationVocabulary;
import org.biopax.validator.impl.CvTermRestriction;
import org.biopax.validator.impl.CvTermsRule;
import org.biopax.validator.impl.CvTermRestriction.UseChildTerms;
import org.springframework.stereotype.Component;


/**
 * Checks:
 * PhysicalEntity.cellularLocation is CellularLocationVocabulary 
 * with valid terms are children of GO "cellular_component".
 * 
 * @author rodche
 */
@Component
public class CellularLocationCvRule extends CvTermsRule<CellularLocationVocabulary> {

	public CellularLocationCvRule() {
		super(CellularLocationVocabulary.class, null, 
				new CvTermRestriction("GO:0005575","GO", false,
						UseChildTerms.ALL, false));
	}

}
