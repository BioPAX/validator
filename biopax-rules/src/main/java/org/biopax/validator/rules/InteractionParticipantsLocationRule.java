package org.biopax.validator.rules;

import java.util.Set;

import org.biopax.paxtools.model.level3.Entity;
import org.biopax.paxtools.model.level3.Interaction;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.model.level3.Transport;
import org.biopax.validator.impl.AbstractRule;
import org.springframework.stereotype.Component;

/**
 * Warning on participants of one interaction 
 * span multiple cellular compartments.
 * For Transport, however, it checks separately on the left and right sides.
 * 
 * @author rodche
 */
@Component
public final class InteractionParticipantsLocationRule extends
		AbstractRule<Interaction> {

	public void check(Interaction thing) {
		Set<Entity> ents = thing.getParticipant();
		if(ents != null) {
		for (Entity e1 : ents) {
				if (e1 instanceof PhysicalEntity 
					&& ((PhysicalEntity) e1).getCellularLocation() != null) 
					{
						for (Entity e2 : ents) {
						if (e2 instanceof PhysicalEntity 
							&& !e1.equals(e2)
							&& !((PhysicalEntity) e1).getCellularLocation()
							.isEquivalent(((PhysicalEntity) e2).getCellularLocation())) 
						{ 
							if(thing instanceof Transport) { // except for transport
								Transport tr = (Transport) thing;
								boolean onDifferentSides = 
									(tr.getLeft().contains(e1) && !tr.getLeft().contains(e2))
									||
									(!tr.getLeft().contains(e1) && tr.getLeft().contains(e2));
								
								if(onDifferentSides) {
									continue;
								}
								// otherwise (diff. locations on the same side), error is to be reported
							}
						
							error(thing, "multiple.location", e1, e2);
						}
					}
				}
			}
		}
	}

	public boolean canCheck(Object thing) {
		return thing instanceof Interaction;
	}

	@Override
	protected void fix(Interaction t, Object... values) {
		// TODO Auto-generated method stub
		
	}

}
