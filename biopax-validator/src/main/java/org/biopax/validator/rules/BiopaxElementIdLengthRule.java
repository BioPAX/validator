package org.biopax.validator.rules;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.validator.api.AbstractRule;
import org.biopax.validator.api.beans.Validation;
import org.springframework.stereotype.Component;

@Component
public class BiopaxElementIdLengthRule extends AbstractRule<BioPAXElement> {
	
	private final static int URI_MAX_LENGTH = 256;
	
	public boolean canCheck(Object thing) {
		return thing instanceof BioPAXElement;
	}

	public void check(final Validation validation, BioPAXElement thing) {
		String rdfid = thing.getRDFId();
		if(rdfid != null && rdfid.length() > URI_MAX_LENGTH)
			error(validation, thing, "too.long.id", false, rdfid.length(), URI_MAX_LENGTH);
	}
	
}
