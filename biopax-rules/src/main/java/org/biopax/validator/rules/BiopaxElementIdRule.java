package org.biopax.validator.rules;

import org.apache.axis.types.NCName;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.validator.impl.AbstractRule;
import org.biopax.validator.utils.BiopaxValidatorUtils;
import org.springframework.stereotype.Component;

@Component
public class BiopaxElementIdRule extends AbstractRule<BioPAXElement> {
	public boolean canCheck(Object thing) {
		return thing instanceof BioPAXElement;
	}

	public void check(BioPAXElement thing) {
		if(thing.getRDFId() != null) {
			String id = BiopaxValidatorUtils.getLocalId(thing);
			if(!NCName.isValid(id))
				error(thing, "invalid.rdf.id", "not a valid NCName: " + id);
		} else
			error(thing, "invalid.rdf.id", "null value");
	}
	
	@Override
	protected void fix(BioPAXElement t, Object... values) {}
}
