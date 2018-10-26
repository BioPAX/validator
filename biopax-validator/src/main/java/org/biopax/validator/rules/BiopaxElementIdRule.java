package org.biopax.validator.rules;


import java.net.URI;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.validator.AbstractRule;
import org.biopax.validator.api.beans.Validation;
import org.springframework.stereotype.Component;

@Component
public class BiopaxElementIdRule extends AbstractRule<BioPAXElement> {
	public boolean canCheck(Object thing) {
		return thing instanceof BioPAXElement;
	}

	public void check(final Validation validation, BioPAXElement thing) {
		String uri = thing.getUri();
		if(uri != null) {
			try {
				URI.create(uri);
			} catch (IllegalArgumentException e) {
				error(validation, thing, "invalid.rdf.id", false,
					String.format("not a valid URI: \"%s\"", uri));
			}	
		} else
			error(validation, thing, "invalid.rdf.id", false, "null value");
	}
	
}
