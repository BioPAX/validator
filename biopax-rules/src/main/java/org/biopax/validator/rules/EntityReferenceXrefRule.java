package org.biopax.validator.rules;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.EntityReference;
import org.biopax.paxtools.model.level3.UnificationXref;
import org.biopax.paxtools.model.level3.Xref;
import org.biopax.validator.impl.AbstractRule;
import org.springframework.stereotype.Component;

/**
 * Checks:
 * EntityReference must have an xref.
 * TODO do NOT add other logic here (better define in a separate rule)
 * 
 * @author rodche
 */
@Component
public class EntityReferenceXrefRule extends AbstractRule<EntityReference> {

   public void check(EntityReference er) {
        if (er.getXref() == null || (er.getXref()).isEmpty()) {
            error(null, "no.xrefs");
        } else {
            boolean present = false;
            for (Xref x : er.getXref()) {
                Class<? extends BioPAXElement> face = x.getModelInterface();
                if (UnificationXref.class.equals(face)) {
                    present = true;
                }
            }
            if (!present) {
                error(er, "no.unification.xref");
            }
        }

    }

	public boolean canCheck(Object thing) {
		return thing instanceof EntityReference;
	}

	@Override
	public void fix(EntityReference t, Object... values) {
	}

}
