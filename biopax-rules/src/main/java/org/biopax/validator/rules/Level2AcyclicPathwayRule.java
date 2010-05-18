package org.biopax.validator.rules;

import javax.annotation.Resource;

import org.biopax.paxtools.controller.AbstractTraverser;
import org.biopax.paxtools.controller.EditorMap;
import org.biopax.paxtools.controller.PropertyEditor;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level2.pathway;
import org.biopax.validator.impl.AbstractRule;
import org.springframework.stereotype.Component;

@Component
public class Level2AcyclicPathwayRule extends AbstractRule<pathway> {
	
	@Resource
	EditorMap editorMap2;
	
	@Override
	public void fix(pathway t, Object... values) {
	}

	public boolean canCheck(Object thing) {
		return thing instanceof pathway;
	}

	public void check(final pathway thing) {
		AbstractTraverser checker = new AbstractTraverser(editorMap2)
	{
			@Override
			protected void visit(Object value, BioPAXElement parent,
					Model model, PropertyEditor editor) {
				if (value instanceof BioPAXElement) {
					if (value instanceof pathway
							&& thing.getRDFId().equalsIgnoreCase(
									((BioPAXElement) value).getRDFId())) {
						error(thing, "cyclic.inclusion",
								getCurrentParentsList().toString());
					}
					traverse((BioPAXElement) value, model);
				}
			}
		};
		
		checker.traverse(thing, null);
	}

}
