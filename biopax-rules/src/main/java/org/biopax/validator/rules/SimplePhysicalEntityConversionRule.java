package org.biopax.validator.rules;

import org.biopax.paxtools.controller.PropertyEditor;
import org.biopax.paxtools.io.simpleIO.SimpleEditorMap;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.Complex;
import org.biopax.paxtools.model.level3.Conversion;
import org.biopax.paxtools.model.level3.SimplePhysicalEntity;
import org.biopax.paxtools.model.level3.SmallMolecule;
import org.biopax.paxtools.util.ClassFilterSet;
import org.biopax.validator.impl.AbstractRule;
import org.biopax.validator.impl.TraverserRunner;
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
	
    public void check(SimplePhysicalEntity protein)
    {
       Set<Conversion> conversions = new HashSet<Conversion>(
			new ClassFilterSet<Conversion>(protein.getParticipantsOf(), Conversion.class));
       
       for(Conversion conversion : conversions) {
    	   String side = 
    		   (conversion.getLeft().contains(protein)) ? "right" : "left";
    	   if(!findProteinOnTheOtherSide(conversion, protein, side)) {
    		   error(protein, "illegal.conversion", conversion, side);
    	   }
       }
    }
    
    boolean findProteinOnTheOtherSide(final Conversion conversion, 
    		final SimplePhysicalEntity prot, final String side) 
    {
       TraverserRunner runner = new TraverserRunner(editorMap3) {
    	    boolean found;
    	    
    		@Override
			protected void visitObjectValue(BioPAXElement value, Model model, PropertyEditor editor) {
    			if(!editor.getProperty().equals(side)) {
    				return; // skip same-side participants
    			}
    			
				if(value instanceof SimplePhysicalEntity
					&& ((SimplePhysicalEntity)value).getEntityReference() != null 
					&& ((SimplePhysicalEntity)value).getEntityReference()
						.isEquivalent(prot.getEntityReference())) 
				{
					found = true;
				} else if (value instanceof Complex){ 
					traverse(value, model);
				}
			}
    		
    		@Override
    		public boolean run(BioPAXElement conversion, Model model) {
    			found = false;
    			super.run(conversion, model);
    			return found;
    		}
    	};
    	
   		return runner.run(conversion, null);
    }
    
    public boolean canCheck(Object thing) {
        return thing instanceof SimplePhysicalEntity 
         && !(thing instanceof SmallMolecule);
    }

    @Override
	protected void fix(SimplePhysicalEntity t, Object... values) {
		// TODO Auto-generated method stub
		
	}
}
