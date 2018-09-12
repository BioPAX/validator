package org.biopax.validator.rules;

/*
 *
 */

import org.biopax.paxtools.model.level3.ProteinReference;
import org.biopax.validator.api.AbstractRule;
import org.biopax.validator.api.beans.Validation;
import org.springframework.stereotype.Component;

/**
 * Checks: non-generic ProteinReference must have organism specified (error otherwise).
 * 
 * @author rodche
 */
@Component
public class ProteinReferenceOrganismRule extends AbstractRule<ProteinReference> 
{
   public void check(final Validation validation, ProteinReference er) {
	 if(er.getMemberEntityReference().isEmpty()) { // is not generic 
       if (er.getOrganism() == null) {
    	   error(validation, er, "cardinality.violated", false, "organism", 1);
       }
	 }
    }

	public boolean canCheck(Object thing) {
		return thing instanceof ProteinReference;
	}
}
