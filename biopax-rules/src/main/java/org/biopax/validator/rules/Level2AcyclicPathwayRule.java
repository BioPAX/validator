package org.biopax.validator.rules;

import org.biopax.paxtools.controller.AbstractTraverser;
import org.biopax.paxtools.controller.PropertyEditor;
import org.biopax.paxtools.util.Filter;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level2.pathway;
import org.biopax.paxtools.model.level2.pathwayStep;
import org.biopax.paxtools.model.level2.process;
import org.biopax.validator.impl.AbstractRule;
import org.biopax.paxtools.controller.SimpleEditorMap;
import org.springframework.stereotype.Component;

@Component
public class Level2AcyclicPathwayRule extends AbstractRule<pathway> 
{
	public boolean canCheck(Object thing) {
		return thing instanceof pathway;
	}

	public void check(final pathway thing, boolean fix) {
		AbstractTraverser checker = new AbstractTraverser(
			SimpleEditorMap.L2, new Filter<PropertyEditor>() {
				@Override
				public boolean filter(PropertyEditor editor) {
					return !"NEXT-STEP".equals(editor.getProperty());
				}
			})
	{
			@Override
			protected void visit(Object value, BioPAXElement parent,
					Model model, PropertyEditor editor) 
			{
				if (value instanceof process 
					|| value instanceof pathwayStep) 
				{
					if (value instanceof pathway
							&& thing.getRDFId().equalsIgnoreCase(
									((BioPAXElement) value).getRDFId())) {
						error(thing, "cyclic.inclusion",
								false, getVisited().toString());
						return;
					}
					traverse((BioPAXElement) value, model);
				}
			}
		};
		
		checker.traverse(thing, null);
	}

}
