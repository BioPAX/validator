package org.biopax.validator.rules;

import java.util.Collection;
import java.util.HashSet;

import org.biopax.paxtools.controller.AbstractTraverser;
import org.biopax.paxtools.controller.PropertyEditor;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level2.pathway;
import org.biopax.validator.impl.AbstractRule;
import org.biopax.validator.utils.BiopaxValidatorUtils;
import org.springframework.stereotype.Component;

/**
 * Checks whether the element is referenced from others.
 * 
 */
@Component
public class Level2DanglingElementRule extends AbstractRule<Model> 
{
	public boolean canCheck(Object thing) {
		return thing instanceof Model
			&& ((Model)thing).getLevel() == BioPAXLevel.L2;
	}

	public void check(Model model, boolean fix) {
		
		// get all the root elements
		final Collection<BioPAXElement> rootElements = 
			new HashSet<BioPAXElement>(model.getObjects());
		
		// extends traverser ;)
		AbstractTraverser checker = new AbstractTraverser(
			BiopaxValidatorUtils.EDITOR_MAP_L2) 
		{
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
		
		// those left are dangling!
		for(BioPAXElement thing : rootElements) {
			if(!(thing instanceof pathway))
				error(thing, "dangling.element", false);
		}
		
	}

}
