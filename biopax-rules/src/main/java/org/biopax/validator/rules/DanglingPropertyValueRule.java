package org.biopax.validator.rules;

import org.biopax.paxtools.controller.AbstractTraverser;
import org.biopax.paxtools.controller.PropertyEditor;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.validator.impl.AbstractRule;
import org.biopax.paxtools.controller.SimpleEditorMap;
import org.springframework.stereotype.Component;

/**
 * Checks whether a BioPAX element's components are present in the model.
 * 
 */
@Component
public class DanglingPropertyValueRule extends AbstractRule<Model> {
	
	public boolean canCheck(Object thing) {
		return thing instanceof Model 
			&& ((Model)thing).getLevel() == BioPAXLevel.L3;
	}

	public void check(Model model, boolean fix) {
		AbstractTraverser traverser = new AbstractTraverser(SimpleEditorMap.L3) {
			@Override
			protected void visit(Object value, BioPAXElement parent, Model model,
					PropertyEditor editor) {
				if(value instanceof BioPAXElement 
						&& !model.contains((BioPAXElement)value)) {
					error(value, "dangling.value", 
						false, editor.getDomain().getSimpleName(), editor.getProperty());
				} 	
			}
		};
		
		// starts from each element in the model and visits its properties
		for(BioPAXElement e: model.getObjects()) {
			traverser.traverse(e, model); 
		}
	}
}
