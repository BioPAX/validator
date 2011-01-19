package org.biopax.validator.rules;

import org.biopax.paxtools.controller.AbstractTraverser;
import org.biopax.paxtools.controller.PropertyEditor;
import org.biopax.paxtools.io.simpleIO.SimpleEditorMap;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.Complex;
import org.biopax.paxtools.model.level3.ComplexAssembly;
import org.biopax.paxtools.model.level3.Conversion;
import org.biopax.paxtools.model.level3.SimplePhysicalEntity;
import org.biopax.paxtools.model.level3.SmallMolecule;
import org.biopax.paxtools.util.ClassFilterSet;
import org.biopax.validator.impl.AbstractRule;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Resource;

/**
 * This rule checks that biopolymers on one side of the conversion 
 * are those modified/relocated on the other side, and not new ones.
 * 
 * User: rodche
 */
@Component
public class SimplePhysicalEntityConversionRule extends AbstractRule<SimplePhysicalEntity>
{

	@Resource
	SimpleEditorMap editorMap3;
	
    public void check(SimplePhysicalEntity protein, boolean fix)
    {
       Set<Conversion> conversions = new HashSet<Conversion>(
			new ClassFilterSet<Conversion>(protein.getParticipantOf(), Conversion.class));
       
       for(Conversion conversion : conversions) {
    	   if(conversion instanceof ComplexAssembly)
    		   continue;
    	   String side = 
    		   (conversion.getLeft().contains(protein)) ? "right" : "left";
    	   if(!findProteinOnTheOtherSide(conversion, protein, side)) {
    		   error(protein, "illegal.conversion", false, conversion, side);
    	   }
       }
    }
    
    boolean findProteinOnTheOtherSide(final Conversion conversion, 
    		final SimplePhysicalEntity prot, final String side) 
    {
    	AbstractTraverser runner = new AbstractTraverser(editorMap3) {
    		@Override
			protected void visit(Object value, BioPAXElement parent, 
					Model model, PropertyEditor editor) {
    			if(!editor.getProperty().equals(side)) {
    				return; // skip same-side participants
    			}
    			
				if(value instanceof SimplePhysicalEntity
					&& ((SimplePhysicalEntity)value).getEntityReference() != null 
					&& ((SimplePhysicalEntity)value).getEntityReference()
						.isEquivalent(prot.getEntityReference())) 
				{
					
					throw new RuntimeException("found!");
					
				} else if (value instanceof Complex){ 
					traverse((Complex) value, model);
				}
			}
    	};
    	
    	try {
    		runner.traverse(conversion, null);
    	} catch (RuntimeException e) {
			return true;
		}
   		return false;
    }
    
    public boolean canCheck(Object thing) {
        return thing instanceof SimplePhysicalEntity 
         && !(thing instanceof SmallMolecule);
    }

}
