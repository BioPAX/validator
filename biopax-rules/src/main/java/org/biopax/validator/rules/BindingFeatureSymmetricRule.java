package org.biopax.validator.rules;

import org.biopax.paxtools.model.level3.BindingFeature;
import org.biopax.validator.impl.AbstractRule;
import org.springframework.stereotype.Component;

/**
 * Checks:
 * BindingFeature.bindsTo is 'symmetrical'.
 * 
 * @author rodche
 */
@Component
public class BindingFeatureSymmetricRule extends AbstractRule<BindingFeature> {

	public boolean canCheck(Object thing) {
		return thing instanceof BindingFeature
			&& ((BindingFeature)thing).getBindsTo() != null;
	}

	public void check(BindingFeature thing, boolean fix) {
		BindingFeature to = thing.getBindsTo();
		if (to != null) {
			if (to.getBindsTo() == null || !thing.equals(to.getBindsTo())) {
				if(!fix) {
					error(thing, "symmetric.violated", "bindsTo", thing
						.getBindsTo());
				} else {
					to.setBindsTo(thing);
				}
			}
		}
	}
	
}
