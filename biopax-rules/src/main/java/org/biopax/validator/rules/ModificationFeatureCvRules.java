package org.biopax.validator.rules;

import java.util.Collection;
import java.util.HashSet;

import org.biopax.paxtools.model.level3.Dna;
import org.biopax.paxtools.model.level3.ModificationFeature;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.model.level3.Protein;
import org.biopax.paxtools.model.level3.Rna;
import org.biopax.validator.impl.AbstractRule;
import org.biopax.validator.impl.CvTermRestriction;
import org.biopax.validator.impl.CvTermsRule;
import org.biopax.validator.impl.CvTermRestriction.UseChildTerms;
import org.springframework.stereotype.Component;

/**
 * Checks:
 * ModificationFeature.modoficationType has valid SequenceModificationVocabulary term,
 * specifically for DNA, RNA or Protein physical entities.
 *
 * @author rodche
 */
@Component
public class ModificationFeatureCvRules extends AbstractRule<ModificationFeature> {

    public boolean canCheck(Object thing) {
    	return thing instanceof ModificationFeature
    		&& ((ModificationFeature)thing).getModificationType() != null;
    }
    
	public void check(ModificationFeature mf) {
		if (mf.getFeatureOf() != null) {
			Collection<Class<?>> parentTypes = new HashSet<Class<?>>();
			for (PhysicalEntity pe : mf.getFeatureOf()) {
				if(parentTypes.contains(pe.getModelInterface())) continue;
				
				CvTermsRule<ModificationFeature> cvRule = null;
				if (pe instanceof Protein) {	
					cvRule = new CvTermsRule<ModificationFeature>(
							ModificationFeature.class, "modificationType", 
							new CvTermRestriction("MI:0118","MI", true, 
									UseChildTerms.ALL, false),
							new CvTermRestriction("MI:0120","MI", true, 
									UseChildTerms.ALL, false)){};
				} else if(pe instanceof Dna || pe instanceof Rna) {
					cvRule = new CvTermsRule<ModificationFeature>(
							ModificationFeature.class, "modificationType", 
							new CvTermRestriction("SO:1000132","SO", true, 
									UseChildTerms.ALL, false),
							new CvTermRestriction("SO:0001059","SO", true, 
									UseChildTerms.ALL, false)){};
				} 
				cvRule.check(mf);
				parentTypes.add(pe.getModelInterface());
			}
		}
	}

	@Override
	protected void fix(ModificationFeature t, Object... values) {
		// TODO Auto-generated method stub
		
	}

 }
