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

	@Override
	public void fix(BindingFeature t, Object... values) {	
	}


	public boolean canCheck(Object thing) {
		// TODO Auto-generated method stub
		return false;
	}

	public void check(BindingFeature thing) {
		BindingFeature to = thing.getBindsTo();
		if(to != null && 
			(to.getBindsTo() == null || !to.getBindsTo().equals(thing)) ) 
		{
			error(thing, "symmetric.violated", "bindsTo", thing.getBindsTo());
		}
		
	}
	
	
}
