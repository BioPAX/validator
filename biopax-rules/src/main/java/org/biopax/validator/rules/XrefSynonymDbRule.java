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

        String primary = xrefHelper.getPrimaryDbName(db);
		if (primary != null && !primary.equals(xrefHelper.dbName(db))) {
			error(x, "db.name.spelling", db, primary);
			fix(x, primary);
		}
			
    }

	@Override
	public void fix(Xref t, Object... values) {
		t.setDb((String) values[0]);
	}
}
