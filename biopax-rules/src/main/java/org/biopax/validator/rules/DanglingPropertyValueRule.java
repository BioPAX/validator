package org.biopax.validator.rules;

import org.biopax.paxtools.controller.AbstractTraverser;
import org.biopax.paxtools.controller.PropertyEditor;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.validator.impl.AbstractRule;
import org.biopax.paxtools.controller.SimpleEditorMap;
import org.springframework.stereotype.Component;

/**
 * Checks whether a BioPAX element's components are present in the model.
 * 
 * This rule is only useful in a BioPAX editor mode/context, i.e.,
 * not in a web app or console app that simply checks BioPAX files!
 * (therefore, its default 'behavior' is better to set to 'ignore')
 * 
 */
@Component
public class DanglingPropertyValueRule extends AbstractRule<Model> {
	
	public boolean canCheck(Object thing) {
		return thing instanceof Model;
	}

	public void check(Model model, boolean fix) {
		AbstractTraverser traverser = new AbstractTraverser(
				SimpleEditorMap.get(model.getLevel())) 
		{
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
