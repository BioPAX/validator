package org.biopax.validator.rules;

import org.biopax.paxtools.model.level3.UnificationXref;
import org.biopax.paxtools.model.level3.XReferrable;
import org.biopax.paxtools.model.level3.Xref;
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
 * 
 * @deprecated
 * (also reported by clonedUtilityClassRule as warning)
 */
@Component
public class UniqueUnificationXrefRule extends AbstractRule<UnificationXref> {

	public boolean canCheck(Object thing) {
		return thing instanceof UnificationXref;
	}
    
	public void check(UnificationXref x) {
        for (XReferrable r : x.getXrefOf()) {
            for (Xref x2 : r.getXref()) {   		
                if (x2 instanceof UnificationXref 
                		&& x != x2 
                		&& x.isEquivalent(x2)) {
                    error(x, "cloned.unification.xref", x2, r);
                }
            }
        }
    }

	@Override
	public void fix(UnificationXref t, Object... values) {
	}
}
