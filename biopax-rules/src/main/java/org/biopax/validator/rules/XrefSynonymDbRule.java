package org.biopax.validator.rules;

import org.biopax.paxtools.model.level3.Xref;
import org.biopax.validator.impl.AbstractRule;
import org.biopax.validator.utils.XrefHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Checks db is the recommended one (look up synonyms)
 *
 * @author rodche
 */
@Component
public class XrefSynonymDbRule extends AbstractRule<Xref>{

    @Autowired
    XrefHelper xrefHelper;

	public boolean canCheck(Object thing) {
		return (thing instanceof Xref);
	}
	
    public void check(Xref x) {
        String db = x.getDb();
        if (db == null) {
        	// another rule will report this 
        	return;
        }

		if (xrefHelper.contains(db) && xrefHelper.hasSynonyms(db)) {
			String primary = xrefHelper.getSynonymsForDbName(db).get(0);
			if (!xrefHelper.dbName(db).equalsIgnoreCase(primary)) {
				error(x, "db.name.spelling", db, primary);
				fix(x, primary);
			}
		}
			
    }

	@Override
	protected void fix(Xref t, Object... values) {
		t.setDb((String) values[0]);
	}
}
