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

import java.util.Arrays;
import java.util.Collection;

import org.biopax.validator.api.AbstractRule;
import org.biopax.validator.api.beans.Validation;
import org.biopax.paxtools.controller.SimpleEditorMap;
import org.biopax.paxtools.controller.AbstractTraverser;
import org.biopax.paxtools.controller.EditorMap;
import org.biopax.paxtools.controller.PropertyEditor;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.Level3Element;
import org.springframework.stereotype.Component;

/**
 * This class warns on empty or weird property values.
 *
 * @author rodche
 *
 */
@Component
public class DataPropertyIllegalValueRule extends AbstractRule<BioPAXElement> {

	private static final Collection<String> warnOnDataPropertyValues = 
			Arrays.asList("0", "-1", "NULL", "NIL");
	
	public boolean canCheck(Object thing) {
		return thing instanceof BioPAXElement;
	}

	@Override
	public void check(final Validation validation, BioPAXElement bpe) {
		EditorMap editorMap = (bpe instanceof Level3Element)
			? SimpleEditorMap.get(BioPAXLevel.L3)
				: SimpleEditorMap.get(BioPAXLevel.L2);
		
		AbstractTraverser checker = new AbstractTraverser(editorMap) {
			@Override
			protected void visit(Object value, BioPAXElement parent,
					Model model, PropertyEditor editor) {
				if (value != null && !(value instanceof BioPAXElement)) {
					if (warnOnDataPropertyValues.contains(value.toString().trim().toUpperCase())) {
						error(validation, parent, "illegal.property.value", validation.isFix(), editor.getProperty(), value);
						if(validation.isFix()) {
							if(editor.isMultipleCardinality())
								editor.removeValueFromBean(value, parent);
							else
								editor.setValueToBean(null, parent);
						}
					}
				}
			}
		};
		
		checker.traverse(bpe, null);
	}
	
}
