package org.biopax.validator.rules;

import org.biopax.paxtools.model.level3.Xref;
import org.biopax.validator.api.AbstractRule;
import org.biopax.validator.api.beans.Validation;
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
	
    public void check(final Validation validation, Xref x) {
        String db = x.getDb();
		if (db != null) { 
			// check db
			String preferedDbName = xrefHelper.getPrimaryDbName(db); 
			if (preferedDbName == null) {
				error(validation, x, "unknown.db", false, db);
				return;
			}
			
			//it's always in uppercase (by XrefHelper design)!
			assert preferedDbName.equals(preferedDbName.toUpperCase());

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
					error(validation, x, "invalid.id.format", false, db, preferedDbName, id, regxp);
					
					// Now - try to fix (in some cases...) 
					while(validation.isFix()) { //- no worries - will use 'break' to escape the infinite loop
						// hack for a "UniProt Isoform" IDs -	
						// (we do this before trying to split id into id and idVersion parts a few lines below)
						if (preferedDbName.startsWith("UNIPROT")) {
							if (xrefHelper.checkIdFormat("UniProt Isoform", id)
								|| xrefHelper.checkIdFormat("UniProt Isoform",id.toUpperCase())) 
							{
								x.setDb("UniProt Isoform");
								x.setId(id.toUpperCase());
								// update the error case, set fixed=true
								error(validation, x, "invalid.id.format", true);
								break;
							} 
						}
							
						// guess, the illegal id is like 'id_ver' or 'id-ver' - split and try then
						int i = id.lastIndexOf('-');
						if(i<0) 
							i = id.lastIndexOf('_');
						if(i<0) 
							i = id.lastIndexOf('.');
						if(i > 0 && i < id.length()) {
							String newId = id.substring(0, i);
							if (xrefHelper.checkIdFormat(preferedDbName, newId)) {
								x.setId(newId);
								x.setIdVersion(id.substring(i + 1));
								// update the error case, set fixed=true there
								error(validation, x, "invalid.id.format", true);
								break;
							}
						}
						
						/* 
						 * Fix if MI:, GO:, MOD:, etc., prefixes were simply missing/forgotten -
						 */
						i = regxp.lastIndexOf(':');
						if(i>0) {
							// guess, regexp looks like "^GO:%d{7}", and we want to get "GO"
							String prefix = regxp.substring(1, i).toUpperCase();
							if (logger.isDebugEnabled())
								logger.debug("Trying to fix id with missing prefix: " + prefix);
							if(preferedDbName.equalsIgnoreCase(xrefHelper.getPrimaryDbName(prefix))
									&& !id.toUpperCase().startsWith(prefix)) 
							{
								String newId = prefix + ':' + id;
								if (xrefHelper.checkIdFormat(preferedDbName, newId)) {
									x.setId(newId);
									error(validation, x, "invalid.id.format", true);
									if (logger.isDebugEnabled())
										logger.debug(x.getModelInterface()
											.getSimpleName() + " " + x
											+ " 'id' auto-fixed! (was: " + id + ")");
									break;
								}
							}
						}
						
						
						/*
						 * Turning ID to upper case can sometimes help (e.g., KEGG, - c00022 to C00022 helps!) - 
						 * because most identifier patterns corresp. to MIRIAM data collections are case sensitive and 
						 * use upper-case symbols (e.g., Uniport's begin with P, Q, O; also - GO:, MOD:, and NP_ - same idea)
						 */
						String newId = id.toUpperCase();
						if (xrefHelper.checkIdFormat(preferedDbName, newId)) {
							x.setId(newId);
							error(validation, x, "invalid.id.format", true);
							if (logger.isDebugEnabled())
								logger.debug(x.getModelInterface()
									.getSimpleName() + " " + x
									+ " 'id' auto-fixed! (was: " + id + ")");
							break;
						}			
						
						
						break; //breaks this loop anyway
					} //end while
				}
			} 
		} 
    }

}
