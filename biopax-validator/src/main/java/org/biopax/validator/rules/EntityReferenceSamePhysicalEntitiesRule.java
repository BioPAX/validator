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

import org.biopax.paxtools.model.level3.EntityReference;
import org.biopax.paxtools.model.level3.SimplePhysicalEntity;
import org.biopax.validator.api.AbstractRule;
import org.biopax.validator.api.beans.Validation;
import org.biopax.validator.utils.Cluster;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Checks PhysicalEntities that reference the same EntityReference 
 * to be in different states (i.e. features on the PhysicalEntity can't be 
 * exactly the same on another PhysicalEntity, e.g. two proteins that 
 * reference the same ProteinReference must have different states.
 * 
 * @author rodche
 */
@Component
public class EntityReferenceSamePhysicalEntitiesRule extends
		AbstractRule<EntityReference> 
{

	Cluster<SimplePhysicalEntity> algorithm = new Cluster<SimplePhysicalEntity>() {
		@Override
		public boolean match(SimplePhysicalEntity e1, SimplePhysicalEntity e2) {
			assert e1.getModelInterface().equals(e2.getModelInterface()); //true as they're both from eref.entityReferenceOf()...
			return  e1.hasEquivalentFeatures(e2) 
					&& e1.hasEquivalentCellularLocation(e2);
			//TODO add 'standardName' for comparison?
		}
	};
	
	
	public void check(final Validation validation, EntityReference eref) 
	{

		Set<Set<SimplePhysicalEntity>> clasters 
			= algorithm.cluster(eref.getEntityReferenceOf(), Integer.MAX_VALUE);
	
		// report the error case once per cluster
		for (Set<SimplePhysicalEntity> col : clasters) 
		{
			if(col.size() > 1) {
				SimplePhysicalEntity u = col.iterator().next();
				col.remove(u);
				error(validation, eref, "same.state.entity", false,	u, col);
			}
		}

	}

	public boolean canCheck(Object thing) {
		return thing instanceof EntityReference;
	}

}
