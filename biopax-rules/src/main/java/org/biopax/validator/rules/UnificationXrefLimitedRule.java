package org.biopax.validator.rules;

import java.util.*;

import javax.annotation.Resource;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.UnificationXref;
import org.biopax.paxtools.model.level3.XReferrable;
import org.biopax.validator.impl.AbstractRule;
import org.biopax.validator.utils.XrefHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * UnificationXref applicability rule
 *
 * @author rodche
 */
@Component
public class UnificationXrefLimitedRule extends AbstractRule<UnificationXref> {

    private Map<Class<BioPAXElement>, String> dbAllow;
    private Map<Class<BioPAXElement>, String> dbDeny;
    private XrefHelper helper;
       
    @Resource(name="dbAllow")
    public void setDbAllow(Map<Class<BioPAXElement>, String> dbAllow) {
		this.dbAllow = dbAllow;
	}
    
    @Resource(name="dbDeny")
    public void setDbDeny(Map<Class<BioPAXElement>, String> dbDeny) {
		this.dbDeny = dbDeny;
	}
    
    
    /**
     * Constructor requires the two sets to be defined in 
     * the Spring application context.
     * 
     * @param xrefHelper
     */
    @Autowired
    public UnificationXrefLimitedRule (XrefHelper xrefHelper) {
    	helper = xrefHelper;
    }
    
    
	public boolean canCheck(Object thing) {
		return thing instanceof UnificationXref;
	}
    
	public void check(UnificationXref x, boolean fix) {
		
		if (x.getDb() == null || helper.getPrimaryDbName(x.getDb())==null) {
			// this rule does not care about invalid databases (names)
			return;
		}

		Collection<String> synonyms = helper.getSynonymsForDbName(x.getDb());
		
		// check constrains for each element containing this unification xref 
		for (XReferrable bpe : x.getXrefOf()) {
			for (Class<BioPAXElement> c : dbAllow.keySet()) {
				if (c.isInstance(bpe)) {
					String dbAllowed = dbAllow.get(c).toLowerCase();
					if (!matched(dbAllowed, synonyms)) {
						error(x, "not.allowed.xref", 
								false, x.getDb(), bpe, c.getSimpleName(), dbAllowed);
					}
				}
			}
			
			for (Class<BioPAXElement> c : dbDeny.keySet()) {
				if (c.isInstance(bpe)) {
					String dbDenied = dbDeny.get(c).toLowerCase();
					if (matched(dbDenied, synonyms)) {
						error(x, "denied.xref", false, x.getDb(), 
							bpe, c.getSimpleName(), dbDenied);
					}
				}
			}

		}
	}
	
	boolean matched(String plainListOfNames, Collection<String> dbNames) {
		for (String db : dbNames) {
			if(plainListOfNames.contains(db.toLowerCase())) {
				return true;
			}
		}
		return false;
	}

}
