package org.biopax.validator.rules;

import org.biopax.paxtools.model.level3.UnificationXref;
import org.biopax.validator.impl.AbstractRule;
import org.springframework.stereotype.Component;

/**
 * Different UnificationXrefs of the same element 
 * must point to different resources
 * (not to be semantically equivalent)
 * 
 * XReferrable, UnificationXref
 * 
 * @author rodche
 */
@Component
public class SharedUnificationXrefRule extends AbstractRule<UnificationXref> {

	public boolean canCheck(Object thing) {
		return thing instanceof UnificationXref;
	}
    
	public void check(UnificationXref x) {
        if(x.getXrefOf().size()>1) {
        	error(x, "shared.unification.xref", x.getXrefOf().toString());
        }
    }

	@Override
	public void fix(UnificationXref t, Object... values) {
		
	}
}
