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

import org.biopax.paxtools.controller.AbstractTraverser;
import org.biopax.paxtools.controller.PropertyEditor;
import org.biopax.paxtools.util.Filter;
import org.biopax.paxtools.controller.SimpleEditorMap;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.Pathway;
import org.biopax.paxtools.model.level3.PathwayStep;
import org.biopax.paxtools.model.level3.Process;
import org.biopax.validator.api.AbstractRule;
import org.biopax.validator.api.beans.Validation;
import org.springframework.stereotype.Component;

// cycles do exist in biological systems...
@Component
public class AcyclicPathwayRule extends AbstractRule<Pathway> {
	
	public boolean canCheck(Object thing) {
		return thing instanceof Pathway;
	}

	public void check(final Validation validation, final Pathway thing) {
		@SuppressWarnings("unchecked")
		AbstractTraverser checker = new AbstractTraverser(
				SimpleEditorMap.L3, 
				new Filter<PropertyEditor>() {
					@Override
					public boolean filter(PropertyEditor editor) {
						return !"nextStep".equals(editor.getProperty());
					}
				}) 
		{	
			@Override
			protected void visit(Object value, BioPAXElement bpe, 
					Model model, PropertyEditor<?,?> editor) 
			{
				if (value instanceof Process
						|| value instanceof PathwayStep) {
					if (value instanceof Pathway && thing.getRDFId()
						.equalsIgnoreCase(((Pathway) value).getRDFId()))
					{
						error(validation, thing,
								"cyclic.inclusion", false, getVisited().toString());
					} 
					else {
						if (log.isTraceEnabled())
							log.trace("Traverse into " + value + " "
									+ value.getClass().getSimpleName());

						traverse((BioPAXElement) value, model);
					}
				}
			}
		};
		
		checker.traverse(thing, null);
	}
	
}
