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
import org.biopax.paxtools.controller.SimpleEditorMap;
import org.biopax.paxtools.controller.Traverser;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.Complex;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.util.Filter;
import org.biopax.validator.api.AbstractRule;
import org.biopax.validator.api.beans.Validation;
import org.springframework.stereotype.Component;


/**
 * Checks:
 * Component's cellularLocation is empty
 * when complex'es is not.
 * 
 * @author rodche
 *
 * TODO check for "same location" taking into account the cell compartments hierarchy?
 */
@Component
public class AcyclicComplexRule extends AbstractRule<Complex> {
	
	public boolean canCheck(Object thing) {
		return thing instanceof Complex;
	}

	public void check(final Validation validation, final Complex thing) {
		final Traverser traverser = new AbstractTraverser(
				SimpleEditorMap.L3, new Filter<PropertyEditor>() {
					//complex.component only
					public boolean filter(PropertyEditor editor) {
						return editor.getProperty().equals("component");
					}
				})
		{
			@Override
			protected void visit(Object range, BioPAXElement domain, Model model,
					PropertyEditor editor) {
				assert range instanceof PhysicalEntity; // - because of filter and mul.cardinality
				if (thing.equals(range)) {
					error(validation, thing, "cyclic.inclusion", false, getVisited().toString());
				} else if(range instanceof Complex) {
					traverse((Complex) range, model);
				}
			}
		};
		
		traverser.traverse(thing, null);
	}


}
