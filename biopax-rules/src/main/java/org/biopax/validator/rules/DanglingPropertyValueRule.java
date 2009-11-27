package org.biopax.validator.rules;

import java.util.Collection;

import javax.annotation.Resource;

import org.biopax.paxtools.controller.EditorMap;
import org.biopax.paxtools.controller.PropertyEditor;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.validator.Validator;
import org.biopax.validator.impl.AbstractRule;
import org.biopax.validator.impl.TraverserRunner;
import org.springframework.stereotype.Component;

/**
 * Checks whether a BioPAX element's components are present in the model.
 * 
 */
@Component
public class DanglingPropertyValueRule extends AbstractRule<Model> {
	
	@Resource
	EditorMap editorMap3;
	
	@Override
	protected void fix(Model t, Object... values) {
		// TODO Auto-generated method stub
	}

	public boolean canCheck(Object thing) {
		return thing instanceof Model 
			&& ((Model)thing).getLevel() == BioPAXLevel.L3;
	}

	public void check(Model model) {
		
		TraverserRunner traverser = new TraverserRunner(editorMap3) {
			@Override
			protected void visitObjectValue(BioPAXElement value, Model model,
					PropertyEditor editor) {
				if(!model.contains(value)) {
					error(value, "dangling.value", 
						editor.getDomain().getSimpleName(), editor.getProperty());
				} 	
			}
		};
		
		// starts from each element in the model and visits its properties
		traverser.run(null, model); 
	}
}
