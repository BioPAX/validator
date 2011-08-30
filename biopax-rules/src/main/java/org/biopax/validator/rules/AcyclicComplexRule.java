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
public class AcyclicComplexRule extends AbstractRule<Complex> {

	static final Filter<PropertyEditor> filter = new Filter<PropertyEditor>() {
		//complex.component only
		public boolean filter(PropertyEditor editor) {
			return editor.getProperty().equals("component");
		}
	};
	
	public boolean canCheck(Object thing) {
		return thing instanceof Complex;
	}

	public void check(final Complex thing, boolean fix) {
		final Traverser traverser = new AbstractTraverser(
				SimpleEditorMap.L3, filter) {
			@Override
			protected void visit(Object range, BioPAXElement domain, Model model,
					PropertyEditor editor) {
				assert range instanceof PhysicalEntity; // - because of filter and mul.cardinality
				if (thing.equals(range)) {
					error(thing, "cyclic.inclusion", false, 
						getVisited().toString());
				} else if(range instanceof Complex) {
					traverse((Complex) range, model);
				}
			}
		};
		
		for(PhysicalEntity pe : thing.getComponent()) {
			traverser.traverse(pe, null);
		}
	}


}
