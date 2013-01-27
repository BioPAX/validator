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

import org.apache.commons.collections15.CollectionUtils;
import org.biopax.paxtools.model.level3.BiochemicalReaction;
import org.biopax.paxtools.model.level3.Complex;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.model.level3.SimplePhysicalEntity;
import org.biopax.paxtools.model.level3.Transport;
import org.biopax.validator.api.AbstractRule;
import org.biopax.validator.api.beans.Validation;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * BiochemicalReaction (base) - the participants on one side don't 
 * change compartments on the other side (otherwise it should be a 
 * TransportWithBiochemicalReaction)
 * 
 * TODO Complexes are matched by name, other physical entities can be "identified" using entity reference.
 * 
 * @author rodche
 */
@Component
public class BiochemReactParticipantsLocationRule extends AbstractRule<BiochemicalReaction>
{

    public void check(final Validation validation, BiochemicalReaction react)
    {
       Set<PhysicalEntity> left = react.getLeft();
       Set<PhysicalEntity> right = react.getRight();
       for (PhysicalEntity lefty : left)
       {
            for (PhysicalEntity righty : right)
            {
            	// PhysicalEntity is either Complex or SimplePhysicalEntity (NucleicAcid, Protein or SmallMolecule)
            	
            	if(!lefty.getModelInterface().equals(righty.getModelInterface())) {
                	continue;
                }
            	
            	boolean sameComplex = (lefty instanceof Complex) // means - righty is also Complex
            		&& CollectionUtils.containsAny(lefty.getName(), righty.getName());
            	// Complex does not have entityReference to match the same kind on both sides...
            	
            	boolean sameSimplePhysicalEntity = (lefty instanceof SimplePhysicalEntity) 
            		&& ( ((SimplePhysicalEntity)lefty).getEntityReference() == null 
            			? ((SimplePhysicalEntity)righty).getEntityReference() == null 
            			: ((SimplePhysicalEntity)lefty).getEntityReference()
            				.isEquivalent(((SimplePhysicalEntity)righty).getEntityReference()) );
            	
            	if(sameComplex || sameSimplePhysicalEntity) {
                	boolean sameLoc = lefty.hasEquivalentCellularLocation(righty);
                	if(!sameLoc	&& !(react instanceof Transport)) {
                		error(validation, react, "participant.location.changed", false, lefty, righty); 
                	} else if(sameLoc && react instanceof Transport) {
                		error(validation, react, "transport.location.same", false, lefty, righty); 
                	}
                } 
            }
        }
    }

    public boolean canCheck(Object thing) {
        return thing instanceof BiochemicalReaction;
    }
}
