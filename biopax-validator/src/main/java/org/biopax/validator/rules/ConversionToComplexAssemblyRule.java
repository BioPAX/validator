package org.biopax.validator.rules;


import org.biopax.paxtools.controller.Fetcher;
import org.biopax.paxtools.controller.PropertyEditor;
import org.biopax.paxtools.controller.SimpleEditorMap;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.util.Filter;
import org.biopax.validator.AbstractRule;
import org.biopax.validator.api.beans.Validation;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * A rule to check if the Conversion can be converted
 * to a ComplexAssembly.
 *
 * Some Conversions are not cast as ComplexAssembly,
 * although there is no modification to the PEs
 * and there is a clear Complex formation
 * throughout the process.
 *
 */
@Component
public class ConversionToComplexAssemblyRule extends AbstractRule<Conversion> {
    public void check(final Validation validation, Conversion thing) {
    	//for thread safety (concurrency) we're using a new fetcher here rather than static one 
    	Fetcher fetcher = new Fetcher(
    			SimpleEditorMap.L3, new Filter<PropertyEditor>() {
    				//complex.component only
    				public boolean filter(PropertyEditor editor) {
    					return editor.getProperty().equals("component");
    				}
    			});   	
        Set<PhysicalEntity> left = new HashSet<PhysicalEntity>(getPEsRecursively(thing.getLeft(), fetcher)); //need a mutable set
        Set<PhysicalEntity> right = getPEsRecursively(thing.getRight(), fetcher);
        left.removeAll(right);

        int complexDiff = getComplexCount(thing.getLeft()) - getComplexCount(thing.getRight());
        if( left.isEmpty()  // when there are no modifications really, but different no. complexes or participants
                && (complexDiff != 0 || thing.getLeft().size() - thing.getRight().size() != 0 ))
            error(validation, thing, "wrong.conversion.class", false, thing.getModelInterface());

    }

    private int getComplexCount(Set<PhysicalEntity> pes) {
        int count = 0;

        for(PhysicalEntity pe: pes) {
            if(pe instanceof Complex)
                count++;
        }

        return count;
    }

    private Set<PhysicalEntity> getPEsRecursively(Set<PhysicalEntity> pes, Fetcher fetcher) {
    	Model m = BioPAXLevel.L3.getDefaultFactory().createModel();
    	for(PhysicalEntity pe : pes) {
    		if(pe instanceof Complex)
    			fetcher.fetch(pe,m);
    		else 
    			if(!m.containsID(pe.getUri()))
    				m.add(pe);
    	}   

    	return m.getObjects(PhysicalEntity.class);
    }

    public boolean canCheck(Object thing) {
        return thing instanceof Conversion
                && !(thing instanceof ComplexAssembly);
    }
}
