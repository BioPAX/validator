package org.biopax.validator.rules;


import org.biopax.paxtools.model.level3.Xref;
import org.biopax.validator.api.AbstractRule;
import org.biopax.validator.api.beans.Validation;
import org.biopax.validator.utils.XrefHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Checks xref.db is one of official db names
 * (warn about known unofficial synonyms and misspelling)
 *
 * @author rodche
 */
@Component
public class XrefSynonymDbRule extends AbstractRule<Xref> {

  @Autowired
  XrefHelper xrefHelper;

  public boolean canCheck(Object thing) {
    return (thing instanceof Xref);
  }

  public void check(final Validation validation, Xref x) {
    String db = x.getDb();
    if (db == null) {
      return; // another (cardinality) rule reports
    }

    String primary = xrefHelper.getPrimaryDbName(db);
    // if primary is null, do nothing, - another rule (XrefRule) reports this
    if (primary != null && !primary.equalsIgnoreCase(db)) {
      // report only if it is definitely not official db synonym
      if (xrefHelper.isUnofficialOrMisspelledDbName(db))
        error(validation, x, "db.name.spelling", validation.isFix(), db, primary);

      // fix, sometimes w/o error message, anyway ;)
      if (validation.isFix()) {
        x.setDb(primary);
      }
    }

  }
}
