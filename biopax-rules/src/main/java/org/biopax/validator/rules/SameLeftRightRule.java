package org.biopax.validator.rules;

import org.biopax.paxtools.model.level3.Conversion;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.validator.impl.AbstractRule;
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

    public void check(Conversion conversion)
    {
       Set<PhysicalEntity> left = conversion.getLeft();
       Set<PhysicalEntity> right = conversion.getRight();
       for (PhysicalEntity lefty : left)
       {
    	    for (PhysicalEntity righty : right)
            {
            	boolean isSame = false;           	
               	isSame = righty.isEquivalent(lefty);
            	if(isSame) {
                   error(conversion, "same.state.participant", lefty, righty);
                }
            }
        }
    }

    public boolean canCheck(Object thing) {
        return thing instanceof Conversion;
    }

    @Override
	protected void fix(Conversion t, Object... values) {
		// TODO Auto-generated method stub
		
	}
}
