package org.biopax.validator.rules;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.validator.impl.AbstractRule;
import org.springframework.stereotype.Component;

@Component
public class BiopaxElementIdLengthRule extends AbstractRule<BioPAXElement> {
	
	private final static int URI_MAX_LENGTH = 256;
	
	public boolean canCheck(Object thing) {
		return thing instanceof BioPAXElement;
	}

	public void check(BioPAXElement thing, boolean fix) {
		String rdfid = thing.getRDFId();
		if(rdfid != null && rdfid.length() > URI_MAX_LENGTH)
			error(thing, "too.long.id", false, rdfid.length(), URI_MAX_LENGTH);
	}
	
}
