package org.biopax.validator.rules;


import org.biopax.paxtools.model.level3.ModificationFeature;
import org.biopax.paxtools.model.level3.SequenceLocation;
import org.biopax.validator.CardinalityAndRangeRule;
import org.springframework.stereotype.Component;

/**
 * Checks: ModificationFeature.featureLocation must be set.
 * @author rodche
 */
@Component
public class ModificationFeatureLocationCRRule extends CardinalityAndRangeRule<ModificationFeature> {
	public ModificationFeatureLocationCRRule() {
		super(ModificationFeature.class, "featureLocation", 1, 1, SequenceLocation.class);
	}
}
