package org.biopax.validator.rules;

import javax.annotation.Resource;

import org.biopax.paxtools.controller.EditorMap;
import org.biopax.paxtools.controller.PropertyEditor;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level2.pathway;
import org.biopax.validator.impl.AbstractRule;
import org.biopax.validator.impl.TraverserRunner;
import org.springframework.stereotype.Component;

@Component
public class Level2AcyclicPathwayRule extends AbstractRule<pathway> {
	
	@Resource
	EditorMap editorMap2;
	
	@Override
	protected void fix(pathway t, Object... values) {
		// TODO Auto-generated method stub
	}

	public boolean canCheck(Object thing) {
		return thing instanceof pathway;
	}

	public void check(pathway thing) {
		TraverserRunner checker = new TraverserRunner(editorMap2)
		{
			@Override
			protected void visitObjectValue(BioPAXElement value, Model model,  PropertyEditor editor) {
				if (value instanceof pathway && 
					start.getRDFId().equalsIgnoreCase(value.getRDFId())) {
						error(start, "cyclic.inclusion", path);
				} 
				traverse(value, model);
			}
		};
		
		checker.run(thing, null);
	}

}
