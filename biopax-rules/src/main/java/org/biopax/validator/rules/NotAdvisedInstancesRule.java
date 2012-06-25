package org.biopax.validator.rules;

import org.biopax.validator.impl.AbstractRule;
import org.biopax.paxtools.model.*;
import org.biopax.paxtools.model.level3.Control;
import org.biopax.paxtools.model.level3.Conversion;
import org.biopax.paxtools.model.level3.EntityFeature;
import org.biopax.paxtools.model.level3.Interaction;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.springframework.stereotype.Component;
import org.biopax.paxtools.model.level2.conversion;
import org.biopax.paxtools.model.level2.physicalEntity;

/**
 * This class warns on instances of too general (top-level) BioPAX classes
 *
 * @author rodche
 *
 */
@Component
public class NotAdvisedInstancesRule extends AbstractRule<BioPAXElement> {

    final Class[] NOT_ADVISED = {
    	Control.class, 
    	Conversion.class,
        Interaction.class,
    	EntityFeature.class, 
    	PhysicalEntity.class, 
    	physicalEntity.class,
    	conversion.class,
    };
    
    private boolean notAllowed(Object o) {
		if (o instanceof BioPAXElement) {
			for (int i = 0; i < NOT_ADVISED.length; i++) {
				if (((BioPAXElement) o).getModelInterface().equals(
						NOT_ADVISED[i])) {
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
		error(thing, "not.specific.element", false, thing.getModelInterface().getSimpleName());
	}

}
