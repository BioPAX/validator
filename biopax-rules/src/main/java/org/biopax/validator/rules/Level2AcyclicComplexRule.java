package org.biopax.validator.rules;


import org.biopax.paxtools.controller.AbstractTraverser;
import org.biopax.paxtools.controller.PropertyEditor;
import org.biopax.paxtools.controller.SimpleEditorMap;
import org.biopax.paxtools.controller.Traverser;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level2.complex;
import org.biopax.paxtools.util.Filter;
import org.biopax.validator.impl.AbstractRule;
import org.springframework.stereotype.Component;


/**
 * Checks:
 * Component's cellularLocation is empty
 * when complex'es is not.
 * 
 * @author rodche
 *
 * TODO check for "same location" taking into account the cell compartments hierarchy?
 */
@Component
public class Level2AcyclicComplexRule extends AbstractRule<complex> {
	
	public boolean canCheck(Object thing) {
		return thing instanceof complex;
	}

	public void check(final complex thing, boolean fix) {
		final Traverser traverser = new AbstractTraverser(
				SimpleEditorMap.L3, new Filter<PropertyEditor>() {
					public boolean filter(PropertyEditor editor) {
						return editor.getProperty().equals("COMPONENTS") 
							|| editor.getProperty().equals("PHYSICAL-ENTITY");
					}
				}) 
		{
			@Override
			protected void visit(Object range, BioPAXElement domain, Model model,
					PropertyEditor editor) {
				if (thing.equals(range)) {
					error(thing, "cyclic.inclusion", false, getVisited().toString());
				} else {
					traverse((BioPAXElement) range, model);
				}
			}
		};
		
		traverser.traverse(thing, null);
	}


}
