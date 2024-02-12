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
			String prefix = xrefUtils.getPrefix(db);
			if (prefix == null) {
				error(validation, x, "unknown.db", false, db);
				return;
			}

			// check id
			String id = x.getId();
			if (id != null) {
				if (!xrefUtils.canCheckIdFormatIn(prefix)) {
					logger.info("Can't check IDs (no regexp) for " + db + " (" + prefix + ")");
				} else if (!xrefUtils.checkIdFormat(prefix, id)) {
					String regxp = xrefUtils.getRegexpString(prefix);
					// report error with fixed=false 
					error(validation, x, "invalid.id.format", false, db, prefix, id, regxp);
					
					// try to fix (in some cases) using a hack
					while(validation.isFix()) { //- no worries - will use 'break' to escape the infinite loop
						// guess it's Uniprot Isoform (next try splitting it into id and idVersion parts)
						if (StringUtils.startsWithIgnoreCase(prefix, "uniprot")) {
							if (id.contains("-")
								&& xrefUtils.checkIdFormat("uniprot.isoform",id.toUpperCase())) {
								x.setDb("uniprot.isoform");
								x.setId(id.toUpperCase());
								// update the error case, set fixed=true
								error(validation, x, "invalid.id.format", true);
								break;
							}
						} // guess it's in fact a PSI-MOD despite PSI-MI is used (todo: likely useless/obsolete code)
						else if (prefix.equalsIgnoreCase("mi")) {
							if (id.toUpperCase().startsWith("MOD")
								&& xrefUtils.checkIdFormat("MOD", id.toUpperCase()))
							{
								x.setDb("mod");
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
							if (xrefUtils.checkIdFormat(prefix, newId)) {
								x.setId(newId);
								x.setIdVersion(id.substring(i + 1));
								// update the error case, set fixed=true there
								error(validation, x, "invalid.id.format", true);
								break;
							}
						}
						
						/* 
						 * Add 'MI:','GO:','MOD:' etc. "banana" to the ID (though it's correct to use w/o that banana/prefix too)
						 */
						i = regxp.lastIndexOf(':');
						if(i>0) {
							// guess, regexp looks like "^GO:%d{7}", and we want to get "GO"
							String p = regxp.substring(1, i).toUpperCase();
							if(prefix.equalsIgnoreCase(xrefUtils.getPrefix(p)) && !id.toUpperCase().startsWith(p)) {
								String newId = p + ':' + id;
								if (xrefUtils.checkIdFormat(prefix, newId)) {
									x.setId(newId);
									error(validation, x, "invalid.id.format", true);
									if (logger.isDebugEnabled()) {
										logger.debug(x.getModelInterface().getSimpleName() + " " + x
												+ " 'id' auto-fixed! (was: " + id + ")");
									}
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
						if (xrefUtils.checkIdFormat(prefix, newId)) {
							x.setId(newId);
							error(validation, x, "invalid.id.format", true);
							if (logger.isDebugEnabled()) {
								logger.debug(x.getModelInterface().getSimpleName() + " " + x + " 'id' auto-fixed! (was: " + id + ")");
							}
							break;
						}

						break; //breaks this loop anyway
					} //end while
				}
			} 
		} 
	}

}
