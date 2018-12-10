package org.biopax.validator.rules;


import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.biopax.paxtools.model.level3.Complex;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.validator.AbstractRule;
import org.biopax.validator.api.beans.Validation;
import org.springframework.stereotype.Component;


/**
 * Checks:
 * Component's cellularLocation is empty
 * when complex'es is not.
 * 
 * @author rodche
 */
@Component
public class ComplexComponentsSameLocationRule extends AbstractRule<Complex> {

	public boolean canCheck(Object thing) {
		return thing instanceof Complex;
	}

	public void check(final Validation validation, Complex thing) {
		if(thing.getCellularLocation() != null) {
			Set<PhysicalEntity> ents = thing.getComponent();
			if (ents != null) {
				Collection<String> comps = new HashSet<String>();
				for (PhysicalEntity e : ents) {
					if (e.getCellularLocation() == null) {
						comps.add(e + " " + e.getCellularLocation());		
					}
				}
				
				if(!comps.isEmpty()) {
					error(validation, thing, 
							"component.no.location", false, thing.getCellularLocation().toString(), comps);
				}
			}
		}
	}

}
