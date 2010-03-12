package org.biopax.validator.rules;

import org.biopax.paxtools.model.level3.Catalysis;
import org.biopax.paxtools.model.level3.Control;
import org.biopax.paxtools.model.level3.ControlType;
import org.biopax.paxtools.model.level3.TemplateReactionRegulation;
import org.biopax.validator.impl.AbstractRule;
import org.springframework.stereotype.Component;

/**
 * Checks:
 * 
 * Catalysis.controlType - only "ACTIVATION"
 * TemplateReactionRegulation.controlType - either "ACTIVATION" or "INHIBITION"
 *
 * @author rodche
 */
@Component
public class ControlTypeRule extends AbstractRule<Control> {

	@Override
	public void fix(Control t, Object... values) {
		if(t instanceof Catalysis) {
			((Catalysis)t).setControlType(ControlType.ACTIVATION);
		} else if (t instanceof TemplateReactionRegulation){
			if(values.length >0 && values[0] instanceof ControlType) {
				ControlType ct = (ControlType) values[0];
				((TemplateReactionRegulation)t).setControlType(ct);
			} else {
				((TemplateReactionRegulation)t).setControlType(null);
				if(logger.isInfoEnabled()) 
					logger.info(t.getRDFId() + 
							" - invalid controlType value deleted");
			}
		} else {
			logger.error("This does not auto-fix " +
					" controlType property of "
					+ t.getModelInterface().getSimpleName());
		}
	}

	public boolean canCheck(Object thing) {
		return thing instanceof Control;
	}

	public void check(Control thing) {
		if(thing.getControlType() != null) 
		{			
			if(thing instanceof Catalysis) {
				Catalysis cat = (Catalysis) thing;
				if(cat.getControlType() != ControlType.ACTIVATION) {
					error(thing, "range.violated", "controlType", 
							cat.getControlType().name(), "",
							ControlType.ACTIVATION.name()
							+ " (or empty)");
					fix(thing);
				}
			} else if(thing instanceof TemplateReactionRegulation) {
				TemplateReactionRegulation trr = (TemplateReactionRegulation) thing;
				if(! (trr.getControlType() == ControlType.ACTIVATION
						|| trr.getControlType() == ControlType.INHIBITION) ) {
					error(thing, "range.violated", "controlType", 
						trr.getControlType().name(), "",
						ControlType.ACTIVATION.name() + " or " + ControlType.INHIBITION.name()
						+ " (or empty)");
					fix(thing);
				}
			}
		}
	}
	
}
