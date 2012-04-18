package org.biopax.validator.rules;

import org.biopax.paxtools.model.level3.Provenance;
import org.biopax.paxtools.model.level3.UnificationXref;
import org.biopax.paxtools.model.level3.Xref;
import org.biopax.validator.impl.AbstractRule;
import org.biopax.validator.utils.XrefHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Checks: Provenance should have valid name and no unification xrefs 
 * (warning)
 * 
 * @deprecated this rule may me invalid or not required...
 * 
 * @author rodche
 */
@Component
public class ProvenanceRule extends AbstractRule<Provenance> {

    @Autowired
    XrefHelper xrefHelper;

    public boolean canCheck(Object thing) {
		return thing instanceof Provenance;
	}  
    
	public void check(Provenance p, boolean fix) {
		// check standardName or displayName is valid
		String db = null;
		if(p.getStandardName() != null)
			db = xrefHelper.getPrimaryDbName(p.getStandardName());
		if (db == null) { // was null or unknown name; try displayName -
			if(p.getDisplayName() != null) {
				db = xrefHelper.getPrimaryDbName(p.getDisplayName());
				if (db == null) {
					error(p, "unknown.db", false, p.getDisplayName() 
							+ " or " + p.getStandardName());
				}
			} else {
				error(p, "cardinality.violated", false, "standardName or displayName", 1);
			}
		}
		
		// check unif.xrefs
		for (Xref x : p.getXref()) {
			if (x instanceof UnificationXref) {
				error(x, "not.allowed.xref", 
					false, x.getDb(), p, "Provenance", "- Miriam or PubMed but not a bioentities db!");
						/*
						if(fix) {
							p.removeXref(x);
						}
						*/
			}
		}
	}

}
