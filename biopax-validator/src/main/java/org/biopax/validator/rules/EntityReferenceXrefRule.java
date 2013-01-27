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
