package org.biopax.validator.rules;

/*
 * #%L
 * BioPAX Validator
 * %%
 * Copyright (C) 2008 - 2013 University of Toronto (baderlab.org) and Memorial Sloan-Kettering Cancer Center (cbio.mskcc.org)
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import org.biopax.paxtools.model.level3.Catalysis;
import org.biopax.paxtools.model.level3.Control;
import org.biopax.paxtools.model.level3.ControlType;
import org.biopax.paxtools.model.level3.TemplateReactionRegulation;
import org.biopax.validator.api.AbstractRule;
import org.biopax.validator.api.beans.Validation;
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

	private void fix(Control t, Object... values) {
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

	public void check(final Validation validation, Control thing) {
		if(thing.getControlType() != null) 
		{			
			if(thing instanceof Catalysis) {
				Catalysis cat = (Catalysis) thing;
				if(cat.getControlType() != ControlType.ACTIVATION) {
					error(validation, thing, "range.violated", 
							validation.isFix(), "controlType",
							cat.getControlType().name(), "", ControlType.ACTIVATION.name()
							+ " (or empty)");
					if(validation.isFix()) {
						fix(thing);
					}
				}
			} else if(thing instanceof TemplateReactionRegulation) {
				TemplateReactionRegulation trr = (TemplateReactionRegulation) thing;
				if(! (trr.getControlType() == ControlType.ACTIVATION
						|| trr.getControlType() == ControlType.INHIBITION) ) 
				{
					error(validation, thing, "range.violated", 
							validation.isFix(), "controlType",
							trr.getControlType().name(), "", ControlType.ACTIVATION.name() + " or " 
							+ ControlType.INHIBITION.name()
							+ " (or empty)");
					if(validation.isFix()) {
						fix(thing);
					}
				}
			}
		}
	}
	
}
