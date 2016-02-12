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


import java.util.Set;

import org.biopax.paxtools.model.level3.Complex;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.model.level3.Stoichiometry;
import org.biopax.validator.api.AbstractRule;
import org.biopax.validator.api.beans.Validation;
import org.springframework.stereotype.Component;


/**
 * Usually complexes have more than 1 component, unless stoichiometry of &gt;1
 * has been set on the single component, as would be done for a homodimer.
 * 
 * @author rodch
 *
 */
@Component
public class ComplexTwoOrMoreParticipantsRule extends AbstractRule<Complex> {

	public boolean canCheck(Object thing) {
		return thing instanceof Complex;
	}

	public void check(final Validation validation, Complex thing) 
	{	
		Set<PhysicalEntity> components = thing.getComponent();	
		
		if(components.isEmpty()) {
			error(validation, thing, "complex.incomplete", false, "no components");
		} else if(components.size()==1) { 
			// one component? - then stoi.coeff. must be > 1 (dimer, trimer,..)
			PhysicalEntity pe = components.iterator().next();
			Set<Stoichiometry> stoi = thing.getComponentStoichiometry();
			String msg = "has one component";
			if(stoi.isEmpty()) {
				error(validation, thing, "complex.incomplete", false, msg + ", but no stoichiometry defined.");
			} else { 
				if(stoi.size() > 1)
					msg += ", but multiple stoichiometries...";
				boolean ok = false;
				for(Stoichiometry s : stoi) {
					if(pe.equals(s.getPhysicalEntity())
							&& s.getStoichiometricCoefficient() > 1) {
						ok = true;
						break;
					}
					
					if(!pe.equals(s.getPhysicalEntity()) && s.getPhysicalEntity() != null) {
						error(validation, thing, "complex.stoichiometry.notcomponent", false, s, s.getPhysicalEntity(), pe);
					}
				}
				if(!ok) {
					error(validation, thing, "complex.incomplete", false, msg + "; which stoichiometry < 2.");
				}
			}
		}

	}

}
