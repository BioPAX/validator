package org.biopax.validator.rules;


import java.util.Set;

import org.biopax.paxtools.model.level3.Complex;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.model.level3.Stoichiometry;
import org.biopax.validator.impl.AbstractRule;
import org.biopax.validator.utils.BiopaxValidatorException;
import org.springframework.stereotype.Component;


/**
 * Usually complexes have more than 1 component, unless stoichiometry of >1 
 * has been set on the single component, as would be done for a homodimer.
 * 
 * @author rodch
 *
 */
@Component
public class ComplexTwoOrMoreParticipantsRule extends AbstractRule<Complex> {

	@Override
	public void fix(Complex t, Object... values) {
	}

	public boolean canCheck(Object thing) {
		return thing instanceof Complex;
	}

	public void check(Complex thing) {
		
		Set<PhysicalEntity> components = thing.getComponent();	
		
		if(components.isEmpty()) {
			error(thing, "complex.incomplete", "no components");
		} else if(components.size()==1) {
			PhysicalEntity pe = components.iterator().next();
			Set<Stoichiometry> stoi = thing.getComponentStoichiometry();
			if(stoi.isEmpty()) {
				error(thing, "complex.incomplete", "no stoichiometry");
			} else {
				boolean ok = false;
				for(Stoichiometry s : stoi) {
					if(s.getPhysicalEntity().equals(pe)
							&& s.getStoichiometricCoefficient() > 1) {
						ok = true;
						break;
					}
					
					if(!s.getPhysicalEntity().equals(pe)) {
						throw new BiopaxValidatorException("Complex " +
								"Stoichiometry contains a physical entity " +
								"that is not in components.", thing, s, s.getPhysicalEntity());
					}
				}
				if(!ok) {
					error(thing, "complex.incomplete", "stoichiometry < 2");
				}
				
			}
		}

	}

}
