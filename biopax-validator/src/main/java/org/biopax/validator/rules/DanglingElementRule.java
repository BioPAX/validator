package org.biopax.validator.rules;


import java.util.Collection;
import java.util.HashSet;


import org.biopax.paxtools.controller.AbstractTraverser;
import org.biopax.paxtools.controller.PropertyEditor;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.Pathway;
import org.biopax.validator.AbstractRule;
import org.biopax.validator.api.beans.Validation;
import org.biopax.paxtools.controller.SimpleEditorMap;
import org.springframework.stereotype.Component;

/**
 * Checks whether the element is referenced from others.
 * 
 */
@Component
public class DanglingElementRule extends AbstractRule<Model> {
	
	public boolean canCheck(Object thing) {
		return thing instanceof Model;
	}

	public void check(final Validation validation, Model model) {
		
		// get all the root elements
		final Collection<BioPAXElement> rootElements = 
			new HashSet<BioPAXElement>(model.getObjects());
		
		// extends traverser ;)
		AbstractTraverser checker = new AbstractTraverser(
				SimpleEditorMap.get(model.getLevel())) 
		{	
			@Override
			protected void visit(Object value, BioPAXElement parent, Model model,
					PropertyEditor editor) {
				if(value instanceof BioPAXElement)
					rootElements.remove(value); // found, i.e., it is used by another element.
			}
		};
		
		// this removes those elements that are referenced from others
		for(BioPAXElement e : model.getObjects()) {
			checker.traverse(e, model);
		}
		
		// those left are in fact dangling!
		for(BioPAXElement thing : rootElements) {
			if(!(thing instanceof Pathway))
				error(validation, thing, "dangling.element", false);
		}
		
	}

}
