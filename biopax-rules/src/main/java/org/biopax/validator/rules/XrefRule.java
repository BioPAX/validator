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
			// check db
			String preferedDbName = xrefHelper.getPrimaryDbName(db);
			if (preferedDbName == null) {
				error(x, "unknown.db", false, db);
				return;
			} 

			// check id
			String id = x.getId();
			if (id != null) {
				if (!xrefHelper.canCheckIdFormatIn(preferedDbName)) {
					if (logger.isWarnEnabled()) {
						logger.warn("Can't check IDs (no regexp) for " 
								+ db + " (" + preferedDbName + ")");
					}
				} else if (!xrefHelper.checkIdFormat(preferedDbName, id)) {
					String regxp = xrefHelper.getRegexpString(preferedDbName);
					// report error with fixed=false 
					error(x, "invalid.id.format", false, db, preferedDbName, id, regxp);
					// now try to correct (only for some...)
					if(fix) {
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
							// update the error case, set fixed=true there
							error(x, "invalid.id.format", true);
						}
						
						// quick fixes
						
						/* trying to generalize the case 
						 * when MI:, GO:, MOD: prefixes are missing..
						 */
						i = regxp.lastIndexOf(':');
						if(i>0) {
							// guess, regexp looks like "^GO:%d{7}", and we want to get "GO:"
							String prefix = regxp.substring(1, i).toUpperCase();
							if (logger.isDebugEnabled())
								logger.debug("Trying to fix id with missing prefix: " + prefix);
							if(preferedDbName.equalsIgnoreCase(xrefHelper.getPrimaryDbName(prefix))
									&& !id.toUpperCase().startsWith(prefix)) 
							{
								x.setId(prefix + ':' + id);
								error(x, "invalid.id.format", true);
								if (logger.isDebugEnabled())
									logger.debug(x.getModelInterface().getSimpleName() 
										+ " " + x + " 'id' auto-fixed! (was: " + id + ")");
							}
						}

					}
				}
			} 
		} 
    }

}
