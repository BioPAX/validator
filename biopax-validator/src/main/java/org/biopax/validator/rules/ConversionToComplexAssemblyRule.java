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

import org.biopax.paxtools.controller.Fetcher;
import org.biopax.paxtools.controller.PropertyEditor;
import org.biopax.paxtools.controller.SimpleEditorMap;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.util.Filter;
import org.biopax.validator.api.AbstractRule;
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
    			if(!m.containsID(pe.getRDFId())) 
    				m.add(pe);
    	}   

    	return m.getObjects(PhysicalEntity.class);
    }

    public boolean canCheck(Object thing) {
        return thing instanceof Conversion
                && !(thing instanceof ComplexAssembly);
    }
}
