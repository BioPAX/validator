package org.biopax.validator.rules;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.EntityReference;
import org.biopax.paxtools.model.level3.UnificationXref;
import org.biopax.paxtools.model.level3.Xref;
import org.biopax.validator.api.AbstractRule;
import org.biopax.validator.api.beans.Validation;
import org.springframework.stereotype.Component;

/**
 * Checks a non-generic EntityReference has a unification xref.
 * 
 * @author rodche
 */
@Component
public class EntityReferenceXrefRule extends AbstractRule<EntityReference> {

   public void check(final Validation validation, EntityReference er) {
	   if(er.getMemberEntityReference().isEmpty()) { // for non-generic ERs only
        if (er.getXref().isEmpty()) {
            error(validation, er, "no.xrefs", false);
        } else {
            boolean present = false;
            for (Xref x : er.getXref()) {
                Class<? extends BioPAXElement> face = x.getModelInterface();
                if (UnificationXref.class.equals(face)) {
                    present = true;
                }
            }
            if (!present) {
                error(validation, er, "no.unification.xref", false);
            }
        }
	   }
    }

	public boolean canCheck(Object thing) {
		return thing instanceof EntityReference;
	}

}
