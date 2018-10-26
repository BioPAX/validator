package org.biopax.validator.rules;


import org.biopax.paxtools.model.level3.BindingFeature;
import org.biopax.validator.CardinalityAndRangeRule;
import org.springframework.stereotype.Component;

/**
 * Checks:
 * BindingFeature 'functional' property constraint.
 * 
 * (We also have to check that 'bindsTo' is inverse functional 
 * and symmetrical, but in a separate rule)
 * 
 * @author rodche
 */
@Component
public class BindingFeatureBindsToCRRule extends CardinalityAndRangeRule<BindingFeature> {
	public BindingFeatureBindsToCRRule() {
		super(BindingFeature.class, "bindsTo", 0, 1, BindingFeature.class);
	}    
}
