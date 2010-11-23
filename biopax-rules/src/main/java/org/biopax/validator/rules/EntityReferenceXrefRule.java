package org.biopax.validator.rules;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.EntityReference;
import org.biopax.paxtools.model.level3.UnificationXref;
import org.biopax.paxtools.model.level3.Xref;
import org.biopax.validator.impl.AbstractRule;
import org.springframework.stereotype.Component;

/**
 * Checks a non-generic EntityReference has a unification xref.
 * 
 * @author rodche
 */
@Component
public class EntityReferenceXrefRule extends AbstractRule<EntityReference> {

   public void check(EntityReference er, boolean fix) {
	   if(er.getMemberEntityReference().isEmpty()) { // for non-generic ERs only
        if (er.getXref().isEmpty()) {
            error(er, "no.xrefs", false);
        } else {
            boolean present = false;
            for (Xref x : er.getXref()) {
                Class<? extends BioPAXElement> face = x.getModelInterface();
                if (UnificationXref.class.equals(face)) {
                    present = true;
                }
            }
            if (!present) {
                error(er, "no.unification.xref", false);
            }
        }
	   }
    }

	public boolean canCheck(Object thing) {
		return thing instanceof EntityReference;
	}

}
