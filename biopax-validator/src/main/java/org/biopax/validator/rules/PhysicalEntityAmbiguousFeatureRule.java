package org.biopax.validator.rules;

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
