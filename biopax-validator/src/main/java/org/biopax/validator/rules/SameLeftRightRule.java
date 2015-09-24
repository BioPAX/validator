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

import org.biopax.paxtools.model.level3.Conversion;
import org.biopax.paxtools.model.level3.EntityReference;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.model.level3.SimplePhysicalEntity;
import org.biopax.validator.api.AbstractRule;
import org.biopax.validator.api.beans.Validation;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * This rule checks if any of the participants on one side of this Conversion remained
 * unchanged on the other side. This is a violation of chemical semantics. 
 * These participants should be listed as controllers instead.
 * 
 * User: demir
 * Date: Apr 22, 2009
 * Time: 3:47:15 PM
 */
@Component
public class SameLeftRightRule extends AbstractRule<Conversion>
{

    public void check(final Validation validation, Conversion conversion)
    {
       Set<PhysicalEntity> left = conversion.getLeft();
       Set<PhysicalEntity> right = conversion.getRight();
       for (PhysicalEntity lefty : left)
       {
    	    for (PhysicalEntity righty : right)
            {
            	boolean isSame = false;           	
               	isSame = righty.isEquivalent(lefty) && lefty.isEquivalent(righty);
            	if(isSame) {
                   // TODO what if both actually have no xrefs, features, i.e., none of "distinguishing" properties? -
            		// a fix/hack - to consider ER's different for this rule purpose only
            		if(lefty instanceof SimplePhysicalEntity) 
            		{	// SimplePhysicalEntity can have an entity reference
            			assert righty instanceof SimplePhysicalEntity; // - righty will be too
            			EntityReference ler = ((SimplePhysicalEntity) lefty).getEntityReference();
            			EntityReference rer = ((SimplePhysicalEntity) righty).getEntityReference();
            			// put this: two PEs having ERs with different RDFID will be considered not equivalent
            			if(ler != null && rer != null && !ler.getUri().equalsIgnoreCase(rer.getUri())) {
            				isSame = false;
            			}
            		}

            		if(isSame)	
            			error(validation, conversion, "same.state.participant", false, lefty, righty);
                }
            }
        }
    }

    public boolean canCheck(Object thing) {
        return thing instanceof Conversion;
    }

}
