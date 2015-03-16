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

import org.biopax.paxtools.model.level3.ComplexAssembly;
import org.biopax.paxtools.model.level3.Conversion;
import org.biopax.paxtools.model.level3.Degradation;
import org.biopax.paxtools.model.level3.Interaction;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.model.level3.SimplePhysicalEntity;
import org.biopax.paxtools.model.level3.SmallMolecule;
import org.biopax.paxtools.util.ClassFilterSet;
import org.biopax.validator.api.AbstractRule;
import org.biopax.validator.api.beans.Validation;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * This rule checks that a biopolymer on one side of a conversion 
 * is that modified/relocated on the other side, and not new one;
 * (two exceptions: ComplexAssembly and Degradation)
 * 
 * User: rodche
 */
@Component
public class SimplePhysicalEntityConversionRule extends AbstractRule<SimplePhysicalEntity>
{
	
    public void check(final Validation validation, SimplePhysicalEntity spe)
    {    	
    	Set<Conversion> conversions = new HashSet<Conversion>(
			new ClassFilterSet<Interaction,Conversion>(
				spe.getParticipantOf(), Conversion.class));
       
    	for(Conversion conversion : conversions) {
    	   if(conversion instanceof ComplexAssembly 
    			   || conversion instanceof Degradation) {
    		   continue; //ignore these conversion types
    	   }
    	   
    	   Set<SimplePhysicalEntity> side = 
    			   new ClassFilterSet<PhysicalEntity,SimplePhysicalEntity>(
    					   conversion.getLeft(), SimplePhysicalEntity.class);
    	   
    	   if(side.contains(spe)) //then compare with the other side
    		   side = new ClassFilterSet<PhysicalEntity,SimplePhysicalEntity>(
					   conversion.getRight(), SimplePhysicalEntity.class);
  
    	   if(!sameKindEntityExists(spe, side))
    		   error(validation, spe, "illegal.conversion", false, conversion);
    	}
    }
    
    boolean sameKindEntityExists(SimplePhysicalEntity spe, Set<SimplePhysicalEntity> side) 
    {    	
    	assert !(spe instanceof SmallMolecule);
    	
    	boolean ret = false;
    	
    	for (SimplePhysicalEntity value : side) 
    	{
			if (!(value instanceof SmallMolecule)) 
			{
				if(value.getEntityReference() != null
					&& value.getEntityReference().isEquivalent(spe.getEntityReference())) 
						return true;
			}
		}
  	
   		return ret;
    }
    
    public boolean canCheck(Object thing) {
        return thing instanceof SimplePhysicalEntity 
         && !(thing instanceof SmallMolecule);
    }

}
