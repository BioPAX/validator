package org.biopax.validator.rules;


import org.biopax.paxtools.model.level3.EntityReference;
import org.biopax.paxtools.model.level3.ModificationFeature;
import org.biopax.paxtools.model.level3.NucleicAcid;
import org.biopax.paxtools.model.level3.NucleicAcidReference;
import org.biopax.paxtools.model.level3.NucleicAcidRegionReference;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.validator.api.CvRestriction;
import org.biopax.validator.api.CvRestriction.UseChildTerms;
import org.biopax.validator.CvTermsRule;
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
				new CvRestriction("SO:1000132","SO", true, 
						UseChildTerms.ALL, false),
				new CvRestriction("SO:0001059","SO", true, 
						UseChildTerms.ALL, false));
	}
	
	// This rule is for Dna*/Rna* and corresp.  entity references's features
	@Override
	public boolean canCheck(Object thing) {
		if ( thing instanceof ModificationFeature
			&& ((ModificationFeature) thing).getModificationType() != null)
		{
			EntityReference er = ((ModificationFeature) thing).getEntityFeatureOf();
			if (er instanceof NucleicAcidReference || er instanceof NucleicAcidRegionReference)
				return true;
			
			for (PhysicalEntity pe : ((ModificationFeature) thing).getFeatureOf()) {
				if (pe instanceof NucleicAcid)
					return true;
			}
		}
		return false;
	}

 }
