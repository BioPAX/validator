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

import java.util.Collection;
import java.util.HashSet;

import org.biopax.paxtools.controller.AbstractTraverser;
import org.biopax.paxtools.controller.PropertyEditor;
import org.biopax.paxtools.util.Filter;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.BioSource;
import org.biopax.paxtools.model.level3.Pathway;
import org.biopax.validator.api.AbstractRule;
import org.biopax.validator.api.beans.Validation;
import org.biopax.paxtools.controller.SimpleEditorMap;
import org.springframework.stereotype.Component;

/**
 * Warn if a pathway and its component have different (not null) 'organism' values.
 * What to do? (ignore, delete this value, or override nested organism properties with pathway's value)
 * 
 * @author rodche
 */
@Component
public class PathwayMultiOrganismRule extends AbstractRule<Pathway> 
{	
	private final static Filter<PropertyEditor> filter = new Filter<PropertyEditor>() {
		@Override
		public boolean filter(PropertyEditor editor) {
			return !"nextStep".equals(editor.getProperty());
		}
	};
	
    public void check(final Validation validation, final Pathway pathway) {
    	final Collection<BioPAXElement> organisms = new HashSet<BioPAXElement>();
    	final BioSource organism = pathway.getOrganism(); // not null - due to the canCheck method!
    	//but..
    	if(organism==null) return; // we do not care
    	
    	AbstractTraverser runner = new AbstractTraverser(
    			SimpleEditorMap.L3, filter) 
    	{
    		@Override
			protected void visit(Object value, BioPAXElement parent, 
					Model model, PropertyEditor editor) 
    		{
				if(value instanceof BioSource) {	
					if(!((BioPAXElement) value).isEquivalent(organism)) {
						organisms.add((BioPAXElement) value);
					}
				} 
				else if (value instanceof BioPAXElement) {
					logger.trace("Traverse into " + value + " "
							+ value.getClass().getSimpleName());
					traverse((BioPAXElement) value, model);
				}
			}
    	};
    	
   		runner.traverse(pathway, null);
   		
		if(organisms.size()>0) {
			error(validation, pathway, "multi.organism.pathway",
				false, organism, organisms);
		}
    }

    public boolean canCheck(Object thing) {
        return thing instanceof Pathway
        	&& ((Pathway)thing).getOrganism() != null;
    }

}
