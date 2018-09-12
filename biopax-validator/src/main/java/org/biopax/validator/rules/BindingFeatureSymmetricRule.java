package org.biopax.validator.rules;

/*
 *
 */

import org.biopax.paxtools.model.level3.BindingFeature;
import org.biopax.validator.api.AbstractRule;
import org.biopax.validator.api.beans.Validation;
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

	public void check(final Validation validation, BindingFeature thing) {
		BindingFeature to = thing.getBindsTo();
		if (to != null) {
			if (to.getBindsTo() == null || !thing.equals(to.getBindsTo())) 
			{	
				error(validation, thing, "symmetric.violated", validation.isFix(), 
						"bindsTo", thing.getBindsTo());
				if(validation.isFix()) {
					to.setBindsTo(thing);
				}
			}
		}
	}
	
}
