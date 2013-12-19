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

import org.biopax.paxtools.model.level3.*;
import org.biopax.validator.api.AbstractRule;
import org.biopax.validator.api.beans.Validation;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * Checks if the stoichiometries of left and right participants matches.
 * TODO clarify; ignore small mols.? etc..
 */
@Component
public class ConversionStoichiometryCheckRule extends AbstractRule<Conversion> {

    public void check(final Validation validation, Conversion thing) {
        float lsto = getStoichiometry(thing.getParticipantStoichiometry(), thing.getLeft()),
              rsto = getStoichiometry(thing.getParticipantStoichiometry(), thing.getRight()),
              diff = Math.abs(lsto - rsto);

        if(diff > 0)
            error(validation, thing, "stoichiometry.mismatch", false, lsto, rsto);
    }

    private int getStoichiometry(Set<Stoichiometry> stois, Set<PhysicalEntity> pes) {
        int total = 0;

        Map<PhysicalEntity, Float> stoiMap = new HashMap<PhysicalEntity, Float>();
        for(Stoichiometry s: stois)
            stoiMap.put(s.getPhysicalEntity(), s.getStoichiometricCoefficient());


        for(PhysicalEntity pe: pes) {

            if(pe instanceof Complex) {
                Complex c = (Complex) pe;
                total += getStoichiometry(c.getComponentStoichiometry(), c.getComponent());
            } else {
                Float k = stoiMap.get(pe);
                if( k == null )
                    k = 1.0f;

                total += k;
            }
        }

        return total;
    }

    public boolean canCheck(Object thing) {
        return thing instanceof Conversion && !(thing instanceof Degradation);
    }
}
