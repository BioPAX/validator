package org.biopax.validator.rules;

import org.biopax.paxtools.model.level3.PhenotypeVocabulary;
import org.biopax.validator.impl.CvTermRestriction;
import org.biopax.validator.impl.CvTermsRule;
import org.biopax.validator.impl.CvTermRestriction.UseChildTerms;
import org.springframework.stereotype.Component;

/**
 * Checks:
 * (GeneticInteraction.phenotype) PhenotypeVocabulary terms.
 *
 * @author rodche
 */
@Component
public class PhenotypeCvRule extends CvTermsRule<PhenotypeVocabulary> {

    public PhenotypeCvRule() {
		super(PhenotypeVocabulary.class, null,
				new CvTermRestriction("PATO:0001995","PATO",true,UseChildTerms.DIRECT,false),
				new CvTermRestriction("PATO:0001894","PATO",true,UseChildTerms.NONE,false),
				new CvTermRestriction("PATO:0001895","PATO",true,UseChildTerms.NONE,false),
				new CvTermRestriction("PATO:0000185","PATO",true,UseChildTerms.NONE,false),
				new CvTermRestriction("PATO:0000188","PATO",true,UseChildTerms.NONE,false),
				new CvTermRestriction("PATO:0002076","PATO",true,UseChildTerms.NONE,false),
				new CvTermRestriction("PATO:0000773","PATO",true,UseChildTerms.NONE,false),
				new CvTermRestriction("PATO:0001434","PATO",false,UseChildTerms.DIRECT,false)
		);
	}

}
