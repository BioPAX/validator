package org.biopax.validator.rules;

/*
 *
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
