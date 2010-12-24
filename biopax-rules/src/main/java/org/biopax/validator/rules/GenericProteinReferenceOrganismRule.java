package org.biopax.validator.rules;

import org.biopax.paxtools.model.level3.ProteinReference;
import org.biopax.validator.impl.AbstractRule;
import org.springframework.stereotype.Component;

/**
 * Checks a generic ProteinReference has organism specified (warning otherwise).
 * 
 * @author rodche
 */
@Component
public class GenericProteinReferenceOrganismRule extends AbstractRule<ProteinReference> 
{
   public void check(ProteinReference er, boolean fix) {
	 if(!er.getMemberEntityReference().isEmpty()) { // is generic 
       if (er.getOrganism() == null) {
    	   error(er, "cardinality.violated", false, "organism", 1);
       }
	 }
    }

	public boolean canCheck(Object thing) {
		return thing instanceof ProteinReference;
	}
}
