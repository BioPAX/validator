package org.biopax.validator.rules;

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

    @Override
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

    @Override
    public boolean canCheck(Object thing) {
        return thing instanceof Conversion && !(thing instanceof Degradation);
    }
}
