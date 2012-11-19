package org.biopax.validator.rules;

import java.net.URI;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.validator.api.AbstractRule;
import org.biopax.validator.api.beans.Validation;
import org.springframework.stereotype.Component;

@Component
public class BiopaxElementIdRule extends AbstractRule<BioPAXElement> {
	public boolean canCheck(Object thing) {
		return thing instanceof BioPAXElement;
	}

	public void check(final Validation validation, BioPAXElement thing) {
		String rdfid = thing.getRDFId();
		if(rdfid != null) {
			try {
				URI.create(rdfid);
			} catch (IllegalArgumentException e) {
				error(validation, thing, "invalid.rdf.id", false, "not a valid URI: " + rdfid);
			}	
		} else
			error(validation, thing, "invalid.rdf.id", false, "null value");
	}
	
}
