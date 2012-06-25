package org.biopax.validator.rules;

import org.biopax.paxtools.controller.AbstractTraverser;
import org.biopax.paxtools.controller.PropertyEditor;
import org.biopax.paxtools.util.Filter;
import org.biopax.paxtools.controller.SimpleEditorMap;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.Pathway;
import org.biopax.paxtools.model.level3.PathwayStep;
import org.biopax.paxtools.model.level3.Process;
import org.biopax.validator.impl.AbstractRule;
import org.springframework.stereotype.Component;

// cycles do exist in biological systems...
@Component
public class AcyclicPathwayRule extends AbstractRule<Pathway> {
	
	public boolean canCheck(Object thing) {
		return thing instanceof Pathway;
	}

	public void check(final Pathway thing, boolean fix) {
		@SuppressWarnings("unchecked")
		AbstractTraverser checker = new AbstractTraverser(
				SimpleEditorMap.L3, 
				new Filter<PropertyEditor>() {
					@Override
					public boolean filter(PropertyEditor editor) {
						return !"nextStep".equals(editor.getProperty());
					}
				}) 
		{	
			@Override
			protected void visit(Object value, BioPAXElement bpe, 
					Model model, PropertyEditor<?,?> editor) 
			{
				if (value instanceof Process
						|| value instanceof PathwayStep) {
					if (value instanceof Pathway && thing.getRDFId()
						.equalsIgnoreCase(((Pathway) value).getRDFId()))
					{
						error(thing, "cyclic.inclusion",
								false, getVisited().toString());
					} 
					else {
						if (log.isTraceEnabled())
							log.trace("Traverse into " + value + " "
									+ value.getClass().getSimpleName());

						traverse((BioPAXElement) value, model);
					}
				}
			}
		};
		
		checker.traverse(thing, null);
	}
	
}
