package org.biopax.validator.rules;


import org.biopax.paxtools.controller.AbstractTraverser;
import org.biopax.paxtools.controller.PropertyEditor;
import org.biopax.paxtools.controller.SimpleEditorMap;
import org.biopax.paxtools.controller.Traverser;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.Complex;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.util.Filter;
import org.biopax.validator.api.AbstractRule;
import org.biopax.validator.api.beans.Validation;
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
public class AcyclicComplexRule extends AbstractRule<Complex> {
	
	public boolean canCheck(Object thing) {
		return thing instanceof Complex;
	}

	public void check(final Validation validation, final Complex thing) {
		final Traverser traverser = new AbstractTraverser(
				SimpleEditorMap.L3, new Filter<PropertyEditor>() {
					//complex.component only
					public boolean filter(PropertyEditor editor) {
						return editor.getProperty().equals("component");
					}
				})
		{
			@Override
			protected void visit(Object range, BioPAXElement domain, Model model,
					PropertyEditor editor) {
				if (thing.equals(range)) {
					error(validation, thing, "cyclic.inclusion", false, "is a component of itself or its componets... : " + domain.getUri());
				} else if(range instanceof Complex) {
					traverse((Complex) range, model);
				}
			}
		};
		
		traverser.traverse(thing, null);
	}

}
