package org.biopax.validator.rules;

import java.net.URI;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.validator.impl.AbstractRule;
import org.springframework.stereotype.Component;

@Component
public class BiopaxElementIdRule extends AbstractRule<BioPAXElement> {
	public boolean canCheck(Object thing) {
		return thing instanceof BioPAXElement;
	}

	public void check(BioPAXElement thing, boolean fix) {
		String rdfid = thing.getRDFId();
		if(rdfid != null) {
			try {
				URI.create(rdfid);
			} catch (IllegalArgumentException e) {
				error(thing, "invalid.rdf.id", "not a valid URI: " + rdfid);
			}	
		} else
			error(thing, "invalid.rdf.id", "null value");
	}
	
}
