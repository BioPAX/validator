package org.biopax.validator.rules;

import org.biopax.paxtools.model.level3.Dna;
import org.biopax.paxtools.model.level3.ModificationFeature;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.model.level3.Rna;
import org.biopax.validator.impl.CvTermRestriction;
import org.biopax.validator.impl.CvTermsRule;
import org.biopax.validator.impl.CvTermRestriction.UseChildTerms;
import org.springframework.stereotype.Component;

/**
 * Checks:
 * Dna's or Rna's modificationFeature.modoficationType has valid SequenceModificationVocabulary term
 * 
 * @author rodche
 */
@Component
public class NucleicAcidModificationFeatureCvRule extends CvTermsRule<ModificationFeature> {
		
	public NucleicAcidModificationFeatureCvRule() {
		super(ModificationFeature.class, "modificationType", 
				new CvTermRestriction("SO:1000132","SO", true, 
						UseChildTerms.ALL, false),
				new CvTermRestriction("SO:0001059","SO", true, 
						UseChildTerms.ALL, false));
	}
	
	// This rule is for Dna/Rna features only
	@Override
	public boolean canCheck(Object thing) {
		if (thing instanceof ModificationFeature
			&& ((ModificationFeature) thing).getModificationType() != null
				&& ((ModificationFeature) thing).getFeatureOf() != null) {
			for (PhysicalEntity pe : ((ModificationFeature) thing).getFeatureOf()) {
				if (pe instanceof Dna || pe instanceof Rna) {
					return true;
				}
			}
		}
		return false;
	}

 }
