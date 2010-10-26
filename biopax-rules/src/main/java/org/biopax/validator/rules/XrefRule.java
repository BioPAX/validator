package org.biopax.validator.rules;

import org.biopax.paxtools.model.level3.Xref;
import org.biopax.validator.impl.AbstractRule;
import org.biopax.validator.utils.XrefHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Validates Xrefs 
 * (db/id properties)
 *
 * @author rodche
 */
@Component
public class XrefRule extends AbstractRule<Xref>{

    @Autowired
    XrefHelper xrefHelper;

	public boolean canCheck(Object thing) {
		return (thing instanceof Xref);
	}
	
    public void check(Xref x, boolean fix) {
        String db = x.getDb();
		if (db != null) { 
			// check db is valid
			String preferedDbName = xrefHelper.getPrimaryDbName(db);
			if (preferedDbName == null) {
				error(x, "unknown.db", db);
				return;
			} 
			else if(!db.equalsIgnoreCase(preferedDbName)
				&& fix == true) {
				x.setDb(preferedDbName);
			}

			String id = x.getId();
			if (id != null) {
				if (!xrefHelper.canCheckIdFormatIn(preferedDbName)) {
					if (logger.isWarnEnabled()) {
						logger.warn("Can't check IDs (no regexp) for " 
								+ db + " (" + preferedDbName + ")");
					}
				} else if (!xrefHelper.checkIdFormat(preferedDbName, id)) {
					if(!fix) {
						error(x, "invalid.id.format", db, id, 
							xrefHelper.getRegexpString(preferedDbName));
					} else {
						// guess, it's like id_ver or id-ver
						int i = id.lastIndexOf('-');
						if(i<0) 
							i = id.lastIndexOf('_');
						if(i<0) 
							i = id.lastIndexOf('.');
						if(i > 0 && i < id.length()) {
							x.setId(id.substring(0, i));
							x.setIdVersion(id.substring(i+1));
							if (logger.isDebugEnabled())
								logger.debug(
									"auto-fix: split id and idVersion for xref: " 
									+ x + " " +  x.getRDFId());
						}
						
						// several quick fixes
						if(db.equalsIgnoreCase("CHEBI") 
							&& !id.toUpperCase().startsWith("CHEBI")) {
							x.setId("CHEBI:" + id);
						}
						
					}
				}
			} 
		} 
    }

}
