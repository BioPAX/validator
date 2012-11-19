package org.biopax.validator.rules;

import org.biopax.paxtools.model.level3.BindingFeature;
import org.biopax.paxtools.model.level3.Complex;
import org.biopax.paxtools.model.level3.ComplexAssembly;
import org.biopax.paxtools.model.level3.Entity;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.util.ClassFilterSet;
import org.biopax.validator.api.AbstractRule;
import org.biopax.validator.api.beans.Validation;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;

/**
 * This rule checks if either ComplexAssembly conversion has a complex on one side,
 * or, at least, several participants are bound to each other on either left or right side. 
 */
@Component
public class ComplexAssemblyHasComplexParticipantRule extends AbstractRule<ComplexAssembly>
{
    public void check(final Validation validation, ComplexAssembly complexAssembly)
    {
		// check if there are any complexes on either side
    	Set<Complex> complexes = 
			new ClassFilterSet<Entity,Complex>(complexAssembly.getParticipant(), Complex.class);
		if(complexes.isEmpty()) {
			boolean bound = isBound(complexAssembly.getRight());
			if(!bound)
				bound = isBound(complexAssembly.getLeft());
			if(!bound)
				error(validation, complexAssembly, "complex.not.present", false);
		}
    }

    /* check "whether participants acquire a binding feature" 
     * would be much more difficult than - "several participants are bound"
     * (e.g., because the former is subject to "the same entityReference" cond...)
     */
    private boolean isBound(Set<PhysicalEntity> participants) {
    	for(PhysicalEntity pe : participants) {
    		//if(pe instanceof Complex) continue; // it's impossible as far isBound called from where it is called
    		if(pe.getFeature() != null 
    				&& pe.getFeature() instanceof BindingFeature) {
    			BindingFeature bf1 = (BindingFeature) pe.getFeature();
    			BindingFeature bf2 = bf1.getBindsTo();
    			if(bf2 != null) {
    				Set<PhysicalEntity> bf2Of = bf2.getFeatureOf();
    				//if these phys. entities are (same side) participants as well
    				if(!Collections.disjoint(participants, bf2Of))
    					return true;
    			}
    		}
    	}
    	return false;
	}

	public boolean canCheck(Object thing) {
        return thing instanceof ComplexAssembly;
    }

}
