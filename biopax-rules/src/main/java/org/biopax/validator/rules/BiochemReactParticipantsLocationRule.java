package org.biopax.validator.rules;

import org.biopax.paxtools.model.level3.BiochemicalReaction;
import org.biopax.paxtools.model.level3.Complex;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.model.level3.SimplePhysicalEntity;
import org.biopax.paxtools.model.level3.Transport;
import org.biopax.validator.impl.AbstractRule;
import org.biopax.validator.utils.BiopaxValidatorUtils;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * BiochemicalReaction (base) - the participants on one side don't 
 * change compartments on the other side (otherwise it should be a 
 * TransportWithBiochemicalReaction)
 * 
 * TODO Complexes are matched by name, other physical entities can be "identified" using entity reference.
 * 
 * @author rodche
 */
@Component
public class BiochemReactParticipantsLocationRule extends AbstractRule<BiochemicalReaction>
{

    public void check(BiochemicalReaction react, boolean fix)
    {
       Set<PhysicalEntity> left = react.getLeft();
       Set<PhysicalEntity> right = react.getRight();
       for (PhysicalEntity lefty : left)
       {
            for (PhysicalEntity righty : right)
            {
            	// PhysicalEntity is either Complex or SimplePhysicalEntity (NucleicAcid, Protein or SmallMolecule)
            	
            	if(!lefty.getModelInterface().equals(righty.getModelInterface())) {
                	continue;
                }
            	
            	boolean sameComplex = (lefty instanceof Complex) // means - righty is also Complex
            		&& !BiopaxValidatorUtils.namesInCommon(lefty, righty).isEmpty(); 
            	// Complex does not have entityReference to match the same kind on both sides...
            	
            	boolean sameSimplePhysicalEntity = (lefty instanceof SimplePhysicalEntity) 
            		&& ( ((SimplePhysicalEntity)lefty).getEntityReference() == null 
            			? ((SimplePhysicalEntity)righty).getEntityReference() == null 
            			: ((SimplePhysicalEntity)lefty).getEntityReference()
            				.isEquivalent(((SimplePhysicalEntity)righty).getEntityReference()) );
            	
            	if(sameComplex || sameSimplePhysicalEntity) {
                	boolean sameLoc = lefty.hasEquivalentCellularLocation(righty);
                	if(!sameLoc	&& !(react instanceof Transport)) {
                		error(react, "participant.location.changed", false, lefty, righty); 
                	} else if(sameLoc && react instanceof Transport) {
                		error(react, "transport.location.same", false, lefty, righty); 
                	}
                } 
            }
        }
    }

    public boolean canCheck(Object thing) {
        return thing instanceof BiochemicalReaction;
    }
}
