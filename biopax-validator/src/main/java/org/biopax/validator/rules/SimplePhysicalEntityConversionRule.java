package org.biopax.validator.rules;

import org.biopax.paxtools.model.level3.Complex;
import org.biopax.paxtools.model.level3.ComplexAssembly;
import org.biopax.paxtools.model.level3.Conversion;
import org.biopax.paxtools.model.level3.Degradation;
import org.biopax.paxtools.model.level3.Interaction;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.model.level3.SimplePhysicalEntity;
import org.biopax.paxtools.model.level3.SmallMolecule;
import org.biopax.paxtools.util.ClassFilterSet;
import org.biopax.validator.api.AbstractRule;
import org.biopax.validator.api.beans.Validation;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * This rule checks that a biopolymer on one side of a conversion 
 * is that modified/relocated on the other side, and not new one;
 * (two exceptions: ComplexAssembly and Degradation)
 * 
 * User: rodche
 */
@Component
public class SimplePhysicalEntityConversionRule extends AbstractRule<SimplePhysicalEntity>
{
	
    public void check(final Validation validation, SimplePhysicalEntity spe)
    {    	
    	Set<Conversion> conversions = new HashSet<Conversion>(
			new ClassFilterSet<Interaction,Conversion>(
				spe.getParticipantOf(), Conversion.class));
       
    	for(Conversion conversion : conversions) {
    	   if(conversion instanceof ComplexAssembly 
    			   || conversion instanceof Degradation) {
    		   continue; //ignore these conversion types
    	   }
    	   Set<PhysicalEntity> side = conversion.getLeft();
    	   if(side.contains(spe))
    		   side = conversion.getRight();
  
    	   if(!sameKindEntityExists(spe, side))
    		   error(validation, spe, "illegal.conversion", false, conversion);
    	}
    }
    
    boolean sameKindEntityExists(SimplePhysicalEntity spe, Set<PhysicalEntity> side) 
    {    	
    	assert !(spe instanceof SmallMolecule);
    	
    	boolean ret = false;
    	
    	for (PhysicalEntity value : side) {
			if (value instanceof SimplePhysicalEntity) {
				SimplePhysicalEntity that = (SimplePhysicalEntity) value;
				if( !(value instanceof SmallMolecule)
					&& that.getEntityReference() != null
					&& that.getEntityReference().isEquivalent(spe.getEntityReference())) 
					return true;
			} else { // Complex
				if(sameKindEntityExists(spe, ((Complex)value).getComponent()))
					return true;
			}
			
	    	//still false - check member PEs as well ;)
	    	if(sameKindEntityExists(spe, value.getMemberPhysicalEntity()))
	    		return true;
		}
  	
   		return ret;
    }
    
    public boolean canCheck(Object thing) {
        return thing instanceof SimplePhysicalEntity 
         && !(thing instanceof SmallMolecule);
    }

}
