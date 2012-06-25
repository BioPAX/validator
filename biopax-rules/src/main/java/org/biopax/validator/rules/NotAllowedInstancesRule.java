package org.biopax.validator.rules;

import org.biopax.validator.impl.AbstractRule;
import org.springframework.stereotype.Component;
import org.biopax.paxtools.model.*;
import org.biopax.paxtools.model.level3.Entity;
import org.biopax.paxtools.model.level3.UtilityClass;
import org.biopax.paxtools.model.level3.Xref;
import org.biopax.paxtools.model.level2.entity;
import org.biopax.paxtools.model.level2.utilityClass;
import org.biopax.paxtools.model.level2.xref;
import org.biopax.paxtools.model.level2.externalReferenceUtilityClass;

/**
 * This class advises on instances of too general (top-level) BioPAX classes
 * (Entity, UtilityClass, etc.)
 *
 * @author rodche
 *
 */
@Component
public class NotAllowedInstancesRule extends AbstractRule<BioPAXElement> {

   final Class[] NOT_ALLOWED = {
        Entity.class,
        UtilityClass.class,
        Xref.class,
	xref.class,
	entity.class,
	utilityClass.class,
	externalReferenceUtilityClass.class
    };

    private boolean notAllowed(BioPAXElement bp) {
		for (int i = 0; i < NOT_ALLOWED.length; i++) {
			if (bp.getModelInterface().equals(NOT_ALLOWED[i])) {
				return true; // found
			}
		}
		return false;
	}
    
	public boolean canCheck(Object thing) {
		if(thing instanceof BioPAXElement) {
			return notAllowed((BioPAXElement) thing);
		} else {
			return false;
		}
	}

	public void check(BioPAXElement thing, boolean fix) {
		error(thing, "not.allowed.element", false, thing.getModelInterface().getSimpleName());
	}
    
}
