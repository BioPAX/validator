package org.biopax.validator.rules;

import javax.annotation.Resource;

import org.biopax.paxtools.controller.AbstractTraverser;
import org.biopax.paxtools.controller.EditorMap;
import org.biopax.paxtools.controller.PropertyEditor;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.Pathway;
import org.biopax.validator.impl.AbstractRule;
import org.springframework.stereotype.Component;

// cycles do exist in biological systems...
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

	public void check(final Pathway thing) {
		AbstractTraverser checker = new AbstractTraverser(editorMap3)
		{
			@Override
			protected void visit(Object value, BioPAXElement bpe, Model model, PropertyEditor editor) {
				if (value instanceof Pathway && 
					thing.getRDFId().equalsIgnoreCase(((Pathway)value).getRDFId())) {
						error(thing, "cyclic.inclusion", getCurrentParentsList().toString());
				} else if(value instanceof BioPAXElement) {
					traverse((BioPAXElement)value, model);
				}
			}
		};
		
		checker.traverse(thing, null);
	}
	
}
