package org.biopax.validator.rules;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.biopax.paxtools.model.level3.Complex;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.validator.impl.AbstractRule;
import org.springframework.stereotype.Component;

/**
 * Warning on complex components
 * span multiple cellular compartments.
 * 
 * @author rodche
 *
 */
@Component
public class ComplexComponentsMultipleLocationRule extends
		AbstractRule<Complex> {

	public void check(Complex thing, boolean fix) {
		Set<PhysicalEntity> ents = thing.getComponent();
		if (ents != null) {
			Collection<String> diffLocs = new HashSet<String>();
			PhysicalEntity ref = thing;
			for (PhysicalEntity e : ents) {
				if(ref.getCellularLocation()==null 
						&& e.getCellularLocation() != null) {
					ref = e;
					continue;
				}
					
				if (e.getCellularLocation() != null
						&& !ref.getCellularLocation().isEquivalent(e.getCellularLocation())) {
					diffLocs.add(e + " " + e.getCellularLocation());		
				}
			}
			
			if(!diffLocs.isEmpty()) {
				error(thing, "component.different.location", 
						false, thing.getCellularLocation() + "", diffLocs);
			}
		}
	}

	public boolean canCheck(Object thing) {
		return thing instanceof Complex;
	}
}
