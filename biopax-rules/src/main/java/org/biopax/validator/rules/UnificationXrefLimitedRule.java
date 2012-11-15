package org.biopax.validator.rules;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Resource;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.UnificationXref;
import org.biopax.paxtools.model.level3.XReferrable;
import org.biopax.validator.result.Validation;
import org.biopax.validator.impl.AbstractRule;
import org.biopax.validator.utils.XrefHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * UnificationXref applicability rule 
 * (BioPAX class - allowed/denied unification xref db names)
 *
 * @author rodche
 */
@Component
public class UnificationXrefLimitedRule extends AbstractRule<UnificationXref> {

    private Map<Class<BioPAXElement>, Set<String>> allow;
    private Map<Class<BioPAXElement>, Set<String>> deny;
    private XrefHelper helper;
    
    private Map<Class<BioPAXElement>, String> dbAllow;
    private Map<Class<BioPAXElement>, String> dbDeny;
    private boolean ready = false;
       
    @Resource(name="dbAllow")
    public void setDbAllow(Map<Class<BioPAXElement>, String> dbAllow) {
		this.dbAllow = new ConcurrentHashMap<Class<BioPAXElement>, String>(dbAllow);
	}
    
    @Resource(name="dbDeny")
    public void setDbDeny(Map<Class<BioPAXElement>, String> dbDeny) {
		this.dbDeny = new ConcurrentHashMap<Class<BioPAXElement>, String>(dbDeny);
	}
    
    // to init on the first check(..) call
    void initInternalMaps() {
    	if(!ready) {
    		this.allow = new ConcurrentHashMap<Class<BioPAXElement>, Set<String>>();
    		for (Class<BioPAXElement> clazz : dbAllow.keySet()) {
    			String[] a = dbAllow.get(clazz).toLowerCase().split(":");
    			final Set<String> allSynonyms = new HashSet<String>();
    			for(String db : a) {
    				Collection<String> synonymsOfDb = helper.getSynonymsForDbName(db);
    				allSynonyms.addAll(synonymsOfDb);
    			}
    			this.allow.put(clazz, allSynonyms);
    		}
    		
    		this.deny = new ConcurrentHashMap<Class<BioPAXElement>, Set<String>>();
    		for (Class<BioPAXElement> clazz : dbDeny.keySet()) {
    			String[] a = dbDeny.get(clazz).toLowerCase().split(":");
    			final Set<String> allSynonyms = new HashSet<String>();
    			for(String db : a) {
    				Collection<String> synonymsOfDb = helper.getSynonymsForDbName(db);
    				allSynonyms.addAll(synonymsOfDb);
    			}
    			this.deny.put(clazz, allSynonyms);
    		}
    		
    		ready = true;
    	}
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
    
	public void check(final Validation validation, UnificationXref x) {
		if(!ready)
			initInternalMaps();
		
		if (x.getDb() == null 
			|| helper.getPrimaryDbName(x.getDb())==null) 
		{
			// ignore for unknown databases (another rule checks)
			return;
		}

		// fix case sensitivity
		final String xdb = helper.dbName(x.getDb());
		
		// check constrains for each element containing this unification xref 
		for (XReferrable bpe : x.getXrefOf()) {
			for (Class<BioPAXElement> c : allow.keySet()) {
				if (c.isInstance(bpe)) {
					if (allow.get(c) != null && !allow.get(c).contains(xdb)) {
						error(validation, x, "not.allowed.xref", false, x.getDb(), 
							bpe, c.getSimpleName(), allow.get(c).toString());
					}
				}
			}
			for (Class<BioPAXElement> c : deny.keySet()) {
				if (c.isInstance(bpe)) {
					if (deny.get(c) != null && deny.get(c).contains(xdb)) {
						error(validation, x, "denied.xref", false, x.getDb(), 
							bpe, c.getSimpleName(), deny.get(c).toString());
					}
				}
			}
		}
	}

}
