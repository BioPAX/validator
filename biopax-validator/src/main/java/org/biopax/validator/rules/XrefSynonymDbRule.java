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
 * Checks xref.db is one of official db names 
 * (warn about known unofficial synonyms and misspelling)
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
			if(validation.isFix()) {
				x.setDb((String) primary);
			}
		}
			
    }
}
