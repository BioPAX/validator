package org.biopax.validator.rules;

/*
 * #%L
 * BioPAX Validator
 * %%
 * Copyright (C) 2008 - 2013 University of Toronto (baderlab.org) and Memorial Sloan-Kettering Cancer Center (cbio.mskcc.org)
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import org.biopax.paxtools.model.level3.EntityReference;
import org.biopax.paxtools.model.level3.ModificationFeature;
import org.biopax.paxtools.model.level3.NucleicAcid;
import org.biopax.paxtools.model.level3.NucleicAcidReference;
import org.biopax.paxtools.model.level3.NucleicAcidRegionReference;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.validator.api.CvRestriction;
import org.biopax.validator.api.CvRestriction.UseChildTerms;
import org.biopax.validator.impl.CvTermsRule;
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
