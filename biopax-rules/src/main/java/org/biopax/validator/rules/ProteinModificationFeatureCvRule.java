package org.biopax.validator.rules;

import org.biopax.paxtools.model.level3.ModificationFeature;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.model.level3.Protein;
import org.biopax.validator.impl.CvTermRestriction;
import org.biopax.validator.impl.Level3CvTermsRule;
import org.biopax.validator.impl.CvTermRestriction.UseChildTerms;
import org.springframework.stereotype.Component;

/**
 * Checks:
 * Protein.modificationFeature.modoficationType has valid SequenceModificationVocabulary term
 * 
 * @author rodche
 */
@Component
public class ProteinModificationFeatureCvRule extends Level3CvTermsRule<ModificationFeature> {
		
	public ProteinModificationFeatureCvRule() {
		super(ModificationFeature.class, "modificationType", 
				new CvTermRestriction("MI:0118","MI", true, 
						UseChildTerms.ALL, false),
				new CvTermRestriction("MI:0120","MI", true, 
						UseChildTerms.ALL, false));
	}
	
	// This rule is for Protein's features only
	@Override
	public boolean canCheck(Object thing) {
		if (thing instanceof ModificationFeature
			&& ((ModificationFeature) thing).getModificationType() != null
				&& ((ModificationFeature) thing).getFeatureOf() != null) {
			for (PhysicalEntity pe : ((ModificationFeature) thing).getFeatureOf()) {
				if (pe instanceof Protein) {
					return true;
				}
			}
		}
		return false;
	}

 }
