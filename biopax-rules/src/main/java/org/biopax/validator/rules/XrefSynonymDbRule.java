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
	
    public void check(Xref x, boolean fix) {
        String db = x.getDb();
        if (db == null) {
        	// another rule will report this 
        	return;
        }

        String primary = xrefHelper.getPrimaryDbName(db);
		if (primary != null && !primary.equals(xrefHelper.dbName(db))) {
			if(fix) {
				// TODO may be report the fact that it has been fixed?.. (requires much re-factoring...)
				x.setDb((String) primary);
			} else {
				error(x, "db.name.spelling", db, primary);
			}
		}
			
    }
}
