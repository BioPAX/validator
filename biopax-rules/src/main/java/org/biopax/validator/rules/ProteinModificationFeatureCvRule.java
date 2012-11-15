package org.biopax.validator.rules;

import org.biopax.paxtools.model.level3.ModificationFeature;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.model.level3.Protein;
import org.biopax.validator.CvRestriction;
import org.biopax.validator.CvRestriction.UseChildTerms;
import org.biopax.validator.impl.Level3CvTermsRule;
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
		/* terms deleted from PSI-MI
		super(ModificationFeature.class, "modificationType", 
				new CvRestriction("MI:0118","MI", true, 
						UseChildTerms.ALL, false),
				new CvRestriction("MI:0120","MI", true, 
						UseChildTerms.ALL, false));
		 */
		//TODO discuss with community, which terms are allowed
		super(ModificationFeature.class, "modificationType",
				new CvRestriction("MOD:01156", "MOD", false, UseChildTerms.ALL, false), 
				new CvRestriction("MOD:01157", "MOD", false, UseChildTerms.ALL, false)
			);
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
