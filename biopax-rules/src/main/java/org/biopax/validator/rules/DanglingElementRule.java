package org.biopax.validator.rules;

import java.util.Collection;
import java.util.HashSet;

import javax.annotation.Resource;

import org.biopax.paxtools.controller.AbstractTraverser;
import org.biopax.paxtools.controller.EditorMap;
import org.biopax.paxtools.controller.PropertyEditor;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.Pathway;
import org.biopax.validator.impl.AbstractRule;
import org.springframework.stereotype.Component;

/**
 * Checks whether the element is referenced from others.
 * 
 */
@Component
public class DanglingElementRule extends AbstractRule<Model> {
	
	@Resource
	EditorMap editorMap3;
	
	public boolean canCheck(Object thing) {
		return thing instanceof Model
			&& ((Model)thing).getLevel() == BioPAXLevel.L3;
	}

	public void check(Model model) {
		
		// get all the root elements
		final Collection<BioPAXElement> rootElements = 
			new HashSet<BioPAXElement>(model.getObjects());
		
		// extends traverser ;)
		AbstractTraverser checker = new AbstractTraverser(editorMap3) {
			
			@Override
			protected void visit(Object value, BioPAXElement parent, Model model,
					PropertyEditor editor) {
				rootElements.remove(value); // found, i.e., is used
			}
		};
		
		// this removes those elements that are referenced from others
		for(BioPAXElement e : model.getObjects()) {
			checker.traverse(e, model);
		}
		
		// those left are in fact dangling!
		for(BioPAXElement thing : rootElements) {
			if(!(thing instanceof Pathway))
				error(thing, "dangling.element");
		}
		
	}

}
