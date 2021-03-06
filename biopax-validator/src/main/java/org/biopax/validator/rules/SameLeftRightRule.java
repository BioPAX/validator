package org.biopax.validator.rules;


import org.biopax.paxtools.model.level3.Conversion;
import org.biopax.paxtools.model.level3.EntityReference;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.model.level3.SimplePhysicalEntity;
import org.biopax.validator.AbstractRule;
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
