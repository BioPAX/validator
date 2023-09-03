package org.biopax.validator.rules;


import org.apache.commons.lang3.StringUtils;
import org.biopax.paxtools.model.level3.Xref;
import org.biopax.validator.AbstractRule;
import org.biopax.validator.api.beans.Validation;
import org.biopax.validator.XrefUtils;
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
  XrefUtils xrefUtils;

	public boolean canCheck(Object thing) {
		return (thing instanceof Xref);
	}
	
	public void check(final Validation validation, Xref x) {

		String db = x.getDb();
		if (db != null) { 
			// check db
			String preferedDbName = xrefUtils.getPrimaryDbName(db);
			if (preferedDbName == null) {
				error(validation, x, "unknown.db", false, db);
				return;
			}

			// check id
			String id = x.getId();
			if (id != null) {
				if (!xrefUtils.canCheckIdFormatIn(preferedDbName)) {
					logger.info("Can't check IDs (no regexp) for " 
							+ db + " (" + preferedDbName + ")");
				} else if (!xrefUtils.checkIdFormat(preferedDbName, id)) {
					
					String regxp = xrefUtils.getRegexpString(preferedDbName);
					// report error with fixed=false 
					error(validation, x, "invalid.id.format", false, db, preferedDbName, id, regxp);
					
					// try to fix (in some cases) using a hack
					while(validation.isFix()) { //- no worries - will use 'break' to escape the infinite loop
						// guess it's a Uniprot Isoform (next try splitting it into id and idVersion parts)
						if (StringUtils.startsWithIgnoreCase(preferedDbName, "UNIPROT")) {
							if (id.contains("-")
								&& xrefUtils.checkIdFormat("uniprot isoform",id.toUpperCase())) {
								x.setDb("uniprot isoform");
								x.setId(id.toUpperCase());
								// update the error case, set fixed=true
								error(validation, x, "invalid.id.format", true);
								break;
							}
						} // guess it's in fact a PSI-MOD despite PSI-MI is used (todo: likely useless/obsolete code)
						else if (preferedDbName.equalsIgnoreCase("MOLECULAR INTERACTIONS ONTOLOGY")) {
							if (id.toUpperCase().startsWith("MOD")
								&& xrefUtils.checkIdFormat("MOD", id.toUpperCase()))
							{
								x.setDb("MOD");
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
							if (xrefUtils.checkIdFormat(preferedDbName, newId)) {
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
							if(preferedDbName.equalsIgnoreCase(xrefUtils.getPrimaryDbName(prefix))
									&& !id.toUpperCase().startsWith(prefix)) 
							{
								String newId = prefix + ':' + id;
								if (xrefUtils.checkIdFormat(preferedDbName, newId)) {
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
						 * because most identifier patterns corresp. to MIRIAM data collections are case-sensitive and
						 * use upper-case symbols (e.g., Uniport's begin with P, Q, O; also - GO:, MOD:, and NP_ - same idea)
						 */
						String newId = id.toUpperCase();
						if (xrefUtils.checkIdFormat(preferedDbName, newId)) {
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
