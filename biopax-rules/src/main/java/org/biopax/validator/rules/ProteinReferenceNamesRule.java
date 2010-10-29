package org.biopax.validator.rules;

import org.biopax.paxtools.model.level3.ProteinReference;
import org.biopax.validator.impl.AbstractRule;
import org.springframework.stereotype.Component;

/**
 * Checks:
 * 
 * Missing names in ProteinReference - it would be useful to set 
 * at least the protein displayName in ProteinReference. 
 * This is mostly for convenience.
 * 
 * @author rodche
 */
@Component
public class ProteinReferenceNamesRule extends AbstractRule<ProteinReference> {

    public void check(ProteinReference pref, boolean fix) {

		if (pref.getName() == null && pref.getStandardName() == null 
				&& pref.getDisplayName() == null) {
			error(pref, "no.short.name", false);
		} 
	}

	public boolean canCheck(Object thing) {
		return thing instanceof ProteinReference;
	}

}
