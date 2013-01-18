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

import org.biopax.paxtools.model.level3.BindingFeature;
import org.biopax.paxtools.model.level3.EntityFeature;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.validator.api.AbstractRule;
import org.biopax.validator.api.beans.Validation;
import org.springframework.stereotype.Component;

import java.util.HashSet;

/**
 *
 * A rule to check if PhysicalEntities that are both participating
 * to a Conversion and a component of a Complex have proper BindingFeature(s).
 * If not, it is hard to deduce whether the PE participates to the Conversion
 * as a component of a Complex or separately?
 *
 */
@Component
public class PhysicalEntityAmbiguousFeatureRule extends AbstractRule<PhysicalEntity>{
    @Override
    public void check(final Validation validation, PhysicalEntity thing) {
        // Capture PEs that is both a participant of a conversion and a component of a complex
        if(!thing.getParticipantOf().isEmpty() && !thing.getComponentOf().isEmpty()) {
            HashSet<EntityFeature> efs = new HashSet<EntityFeature>();

            efs.addAll(thing.getFeature());
            efs.addAll(thing.getNotFeature());

            // Do we have any information about the binding properties? If so, it's OK.
            for(EntityFeature ef: efs) {
                if(ef instanceof BindingFeature) {
                    return;
                }
            }

            // TODO: Call Emek's feature resolver function explicitly if the PE is to be fixed

            error(validation, thing, "ambiguous.feature", false, thing.getName());
        }
    }

    @Override
    public boolean canCheck(Object thing) {
        return thing instanceof PhysicalEntity;
    }
}
