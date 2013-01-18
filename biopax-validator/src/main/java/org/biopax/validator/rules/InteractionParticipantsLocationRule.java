package org.biopax.validator.rules;

/*
 * #%L
 * BioPAX Validator
 * %%
 * Copyright (C) 2008 - 2013 University of Toronto (baderlab.org) and Memorial Sloan-Kettering Cancer Center (cbio.mskcc.org)
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import java.util.Set;

import org.biopax.paxtools.model.level3.Entity;
import org.biopax.paxtools.model.level3.Interaction;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.model.level3.TemplateReaction;
import org.biopax.paxtools.model.level3.Transport;
import org.biopax.validator.api.AbstractRule;
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
