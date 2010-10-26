package org.biopax.validator.rules;

import org.biopax.validator.impl.AbstractRule;
import org.biopax.paxtools.model.*;
import org.biopax.paxtools.model.level3.Control;
import org.biopax.paxtools.model.level3.EntityFeature;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.springframework.stereotype.Component;
import org.biopax.paxtools.model.level2.physicalEntity;

/**
 * This class warns on instances of too general (top-level) BioPAX classes
 *
 * @author rodche
 *
 */
@Component
public class NotAdvisedInstancesRule extends AbstractRule<BioPAXElement> {

    static final Class[] NOT_ALLOWED = {
    	Control.class, 
    	EntityFeature.class, 
    	PhysicalEntity.class, 
    	physicalEntity.class
    };
    
    private boolean notAllowed(Object o) {
		if (o instanceof BioPAXElement) {
			for (int i = 0; i < NOT_ALLOWED.length; i++) {
				if (((BioPAXElement) o).getModelInterface().equals(
						NOT_ALLOWED[i])) {
					return true; // found
				}
			}
		}
		return false;
	}
    
	public boolean canCheck(Object thing) {
		return notAllowed(thing);
	}

	public void check(BioPAXElement thing, boolean fix) {
		error(thing, "not.specific.element", thing.getModelInterface().getSimpleName());
	}

}
