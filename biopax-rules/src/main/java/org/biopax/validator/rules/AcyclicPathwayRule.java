package org.biopax.validator.rules;

import javax.annotation.Resource;

import org.biopax.paxtools.controller.EditorMap;
import org.biopax.paxtools.controller.PropertyEditor;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.Pathway;
import org.biopax.validator.impl.AbstractRule;
import org.biopax.validator.impl.TraverserRunner;
import org.springframework.stereotype.Component;

@Component
public class AcyclicPathwayRule extends AbstractRule<Pathway> {

	@Resource
	EditorMap editorMap3;
	
	@Override
	protected void fix(Pathway t, Object... values) {
		// TODO Auto-generated method stub
	}

	public boolean canCheck(Object thing) {
		return thing instanceof Pathway;
	}

	public void check(Pathway thing) {
		TraverserRunner checker = new TraverserRunner(editorMap3)
		{
			@Override
			protected void visitObjectValue(BioPAXElement value, Model model, PropertyEditor editor) {
				if (value instanceof Pathway && 
					start.getRDFId().equalsIgnoreCase(value.getRDFId())) {
						error(start, "cyclic.inclusion", path);
				} 
				traverse(value, model);
			}
		};
		
		checker.run(thing, null);
	}
	
}
