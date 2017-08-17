package org.biopax.validator.rules;

/*
 * #%L
 * BioPAX Validator
 * %%
 * Copyright (C) 2008 - 2013 University of Toronto (baderlab.org) and Memorial Sloan-Kettering Cancer Center (cbio.mskcc.org)
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

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
					logger.info("Can't check IDs (no regexp) for " 
							+ db + " (" + preferedDbName + ")");
				} else if (!xrefHelper.checkIdFormat(preferedDbName, id)) {
					
					String regxp = xrefHelper.getRegexpString(preferedDbName);
					// report error with fixed=false 
					error(validation, x, "invalid.id.format", false, db, preferedDbName, id, regxp);
					
					// Now - try to fix (in some cases...) 
					while(validation.isFix()) { //- no worries - will use 'break' to escape the infinite loop
						// guess it's a "UniProt Isoform" ID -	
						// (do before we next will try splitting it into id and idVersion parts)
						if (preferedDbName.startsWith("UNIPROT")) {
							if (id.contains("-")
								&& xrefHelper.checkIdFormat("UniProt Isoform",id.toUpperCase())) 
							{
								x.setDb("UniProt Isoform");
								x.setId(id.toUpperCase());
								// update the error case, set fixed=true
								error(validation, x, "invalid.id.format", true);
								break;
							} 
						} // guess it's in fact a PSI-MOD despite PSI-MI is used
						else if (preferedDbName.equalsIgnoreCase("MOLECULAR INTERACTIONS ONTOLOGY")) {
							if (id.toUpperCase().startsWith("MOD")
								&& xrefHelper.checkIdFormat("MOD", id.toUpperCase())) 
							{
								x.setDb("PSI-MOD");
								x.setId(id.toUpperCase());
								// update the error case, set fixed=true
								error(validation, x, "invalid.id.format", true);
								break;
							} 
						}
							
						// guess, the illegal id is like 'id_ver' and split then -
						int i = id.lastIndexOf('.');
						if(i<0) 
							i = id.lastIndexOf('_');
						if(i<0) 
							i = id.lastIndexOf('-');
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
