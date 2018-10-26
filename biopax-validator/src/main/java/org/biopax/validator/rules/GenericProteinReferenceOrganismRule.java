package org.biopax.validator.rules;


import org.biopax.paxtools.model.level3.ProteinReference;
import org.biopax.validator.AbstractRule;
import org.biopax.validator.api.beans.Validation;
import org.springframework.stereotype.Component;

/**
 * Checks a generic ProteinReference has organism specified (warning otherwise).
 * 
 * @author rodche
 */
@Component
public class GenericProteinReferenceOrganismRule extends AbstractRule<ProteinReference> 
{
   public void check(final Validation validation, ProteinReference er) {
	 if(!er.getMemberEntityReference().isEmpty()) { // is generic 
       if (er.getOrganism() == null) {
    	   error(validation, er, "min.cardinality.violated", false, "organism", 1);
       }
	 }
    }

	public boolean canCheck(Object thing) {
		return thing instanceof ProteinReference;
	}
}
