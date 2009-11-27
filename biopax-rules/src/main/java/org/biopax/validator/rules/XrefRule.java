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
	
    public void check(Xref x) {
        String db = x.getDb();
		if (db != null) { 
			// check db is valid
			if (!xrefHelper.contains(db)) {
				error(x, "unknown.db", db);
				return;
			}

			String id = x.getId();
			if (id != null) {
				if (!xrefHelper.canCheckIdFormatIn(db)) {
					if (logger.isWarnEnabled()) {
						logger.warn("Can't check IDs (no regexp) for " + db);
					}
				} else if (!xrefHelper.checkIdFormat(db, id)) {
					error(x, "invalid.id.format", db, id, xrefHelper
							.getRegexpString(db));
				}
			} 
		} 
    }

	@Override
	protected void fix(Xref t, Object... values) {
	}
}
