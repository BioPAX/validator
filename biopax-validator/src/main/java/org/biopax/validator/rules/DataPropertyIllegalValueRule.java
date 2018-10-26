package org.biopax.validator.rules;


import java.util.Arrays;
import java.util.Collection;

import org.biopax.validator.AbstractRule;
import org.biopax.validator.api.beans.Validation;
import org.biopax.paxtools.controller.PrimitivePropertyEditor;
import org.biopax.paxtools.controller.SimpleEditorMap;
import org.biopax.paxtools.controller.EditorMap;
import org.biopax.paxtools.controller.PropertyEditor;
import org.biopax.paxtools.controller.StringPropertyEditor;
import org.biopax.paxtools.controller.Traverser;
import org.biopax.paxtools.controller.Visitor;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.Level3Element;
import org.biopax.paxtools.util.Filter;
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
			Arrays.asList("0", "-1", "NULL", "NIL", "NONE", "N/A");
	
	public boolean canCheck(Object thing) {
		return thing instanceof BioPAXElement;
	}

	public void check(final Validation validation, BioPAXElement bpe) {
		EditorMap editorMap = (bpe instanceof Level3Element)
			? SimpleEditorMap.get(BioPAXLevel.L3)
				: SimpleEditorMap.get(BioPAXLevel.L2);
		
		Traverser checker = new Traverser(editorMap, new Visitor() {
			@Override
			public void visit(BioPAXElement domain, Object range, Model model, PropertyEditor editor) {
				if (range != null) {
					if (warnOnDataPropertyValues.contains(range.toString().trim().toUpperCase())) {
						error(validation, domain, "illegal.property.value", 
								validation.isFix(), editor.getProperty(), range);
						if(validation.isFix()) {
							if(editor.isMultipleCardinality())
								editor.removeValueFromBean(range, domain);
							if(!editor.isMultipleCardinality())
								editor.setValueToBean(null, domain);
						}
					}
				}
			}
		}, new Filter<PropertyEditor> () {
			@Override
			public boolean filter(PropertyEditor ed) {
				return (ed instanceof PrimitivePropertyEditor)
						|| (ed instanceof StringPropertyEditor);
			}
			
		});
		
		checker.traverse(bpe, null);
	}
	
}
