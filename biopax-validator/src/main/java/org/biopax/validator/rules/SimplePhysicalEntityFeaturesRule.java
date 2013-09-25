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

import org.biopax.paxtools.model.level3.EntityFeature;
import org.biopax.paxtools.model.level3.EntityReference;
import org.biopax.paxtools.model.level3.SimplePhysicalEntity;
import org.biopax.validator.api.AbstractRule;
import org.biopax.validator.api.beans.Validation;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * A rule to check if a Simple Physical Entity Feature is also a feature of
 * its Entity Reference. If not, fixes it by adding the missing feature
 * to the Entity Reference.
 *
 */
@Component
public class SimplePhysicalEntityFeaturesRule extends AbstractRule<SimplePhysicalEntity>{

    @Override
    public void check(final Validation validation, SimplePhysicalEntity thing) {
        EntityReference er = thing.getEntityReference();
        //wrap er.getEntityFeature() in a new hashset because it can be modified (also in other threads)
        Set<EntityFeature> erefs =  new HashSet<EntityFeature>(er.getEntityFeature());
        Set<EntityFeature> peefs = new HashSet<EntityFeature>();

        peefs.addAll(thing.getFeature());
        peefs.addAll(thing.getNotFeature());

        for(EntityFeature ef: peefs) {
            if(!erefs.contains(ef)) {
                if(validation.isFix())
                    er.addEntityFeature(ef);

                error(validation, thing, "improper.feature.use", validation.isFix(), ef.getRDFId(), er.getRDFId());
            }
        }
    }

    @Override
    public boolean canCheck(Object thing) {
        return thing instanceof SimplePhysicalEntity && ((SimplePhysicalEntity) thing).getEntityReference() != null;
    }
}
