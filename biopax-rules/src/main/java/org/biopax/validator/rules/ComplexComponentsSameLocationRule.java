package org.biopax.validator.rules;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.biopax.paxtools.model.level3.Complex;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.validator.impl.AbstractRule;
import org.springframework.stereotype.Component;


/**
 * Checks:
 * Component's cellularLocation is empty
 * when complex'es is not.
 * 
 * @author rodch
 *
 * TODO check for "same location" taking into account the cell compartments hierarchy?
 */
@Component
public class ComplexComponentsSameLocationRule extends AbstractRule<Complex> {
	
	@Override
	protected void fix(Complex t, Object... values) {
		// TODO Auto-generated method stub

	}

	public boolean canCheck(Object thing) {
		return thing instanceof Complex;
	}

	public void check(Complex thing) {
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
					error(thing, "component.no.location", 
							thing.getCellularLocation().toString(), comps);
				}
			}
		}
	}

}
