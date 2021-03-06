package org.biopax.validator.rules;


import java.util.Set;

import org.biopax.paxtools.model.level3.Entity;
import org.biopax.paxtools.model.level3.Interaction;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.model.level3.TemplateReaction;
import org.biopax.paxtools.model.level3.Transport;
import org.biopax.validator.AbstractRule;
import org.biopax.validator.api.beans.Validation;
import org.springframework.stereotype.Component;

/**
 * Warning on participants of one interaction span multiple cellular compartments.
 * For Transport however, it checks separately on the left and right sides, 
 * and also it does not check for TemplateReaction.
 * 
 * @author rodche
 */
@Component
public class InteractionParticipantsLocationRule extends
		AbstractRule<Interaction> {

	public void check(final Validation validation, Interaction thing) {
		// exclude template reactions (second time, - after canCheck ;))
		if(thing instanceof TemplateReaction)
			return;
		
		Set<Entity> ents = thing.getParticipant();
		if(ents != null) {
		for (Entity e1 : ents) {
			if (e1 instanceof PhysicalEntity) 
			{
				for (Entity e2 : ents) 
				{
					if (e2 instanceof PhysicalEntity && !e1.equals(e2)
					&& !((PhysicalEntity) e1).hasEquivalentCellularLocation((PhysicalEntity) e2)) 
					{ 
						if(thing instanceof Transport) 
						{ // it's a bit different rule for the transport
							Transport tr = (Transport) thing;
							boolean onDifferentSides = 
								(tr.getLeft().contains(e1) && !tr.getLeft().contains(e2))
								||
								(!tr.getLeft().contains(e1) && tr.getLeft().contains(e2));
							
							if(onDifferentSides) {
								continue; // no error
							}
						}
						// report error
						error(validation, thing, "multiple.location", false, e1, e2);
					  }
					}
				}
			}
		}
	}

	public boolean canCheck(Object thing) {
		return thing instanceof Interaction 
			&& !(thing instanceof TemplateReaction);
	}

}
