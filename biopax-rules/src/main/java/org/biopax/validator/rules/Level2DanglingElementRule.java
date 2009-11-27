package org.biopax.validator.rules;

import java.util.Collection;
import java.util.HashSet;

import javax.annotation.Resource;

import org.biopax.paxtools.controller.EditorMap;
import org.biopax.paxtools.controller.PropertyEditor;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level2.pathway;
import org.biopax.validator.impl.AbstractRule;
import org.biopax.validator.impl.TraverserRunner;
import org.springframework.stereotype.Component;

/**
 * Checks whether the element is referenced from others.
 * 
 */
@Component
public class Level2DanglingElementRule extends AbstractRule<Model> {
	
	@Resource
	EditorMap editorMap2;
	
	@Override
	protected void fix(Model t, Object... values) {
		// TODO Auto-generated method stub
	}

	public boolean canCheck(Object thing) {
		return thing instanceof Model
			&& ((Model)thing).getLevel() == BioPAXLevel.L2;
	}

	public void check(Model model) {
		
		// get all the root elements
		final Collection<BioPAXElement> rootElements = 
			new HashSet<BioPAXElement>(model.getObjects());
		
		// extends traverser ;)
		TraverserRunner checker = new TraverserRunner(editorMap2) {
			@Override
			protected void visitObjectValue(BioPAXElement value, Model model,
					PropertyEditor editor) {
				rootElements.remove(value); // found, i.e., is used
			}
		};
		
		// this removes those elements that are referenced from others
		checker.run(null, model); 
		
		// those left are dangling!
		for(BioPAXElement thing : rootElements) {
			if(!(thing instanceof pathway))
				error(thing, "dangling.element");
		}
		
	}

}
