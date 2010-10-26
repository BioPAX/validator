package org.biopax.validator.rules;

import javax.annotation.Resource;

import org.biopax.paxtools.controller.AbstractTraverser;
import org.biopax.paxtools.controller.EditorMap;
import org.biopax.paxtools.controller.PropertyEditor;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.validator.impl.AbstractRule;
import org.springframework.stereotype.Component;

/**
 * Checks whether its components are present in the model.
 * 
 */
@Component
public class Level2DanglingPropertyValueRule extends AbstractRule<Model> {
	
	@Resource
	EditorMap editorMap2;
	
	public boolean canCheck(Object thing) {
		return thing instanceof Model 
			&& ((Model)thing).getLevel() == BioPAXLevel.L2;
	}

	public void check(Model model, boolean fix) {	
		AbstractTraverser traverser = new AbstractTraverser(editorMap2) {
			@Override
			protected void visit(Object value, BioPAXElement parent, Model model,
					PropertyEditor editor) {
				if(value instanceof BioPAXElement && !model.contains((BioPAXElement) value)) {
					error(value, "dangling.value", 
						editor.getDomain().getSimpleName(), editor.getProperty());
				} 	
			}
		};
		
		// starts from each element in the model and visits its properties
		for(BioPAXElement e : model.getObjects()) {
			traverser.traverse(e, model); 
		}
	}
}
