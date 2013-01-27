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


import org.biopax.paxtools.model.level3.Named;
import org.biopax.paxtools.model.level3.Provenance;
import org.biopax.validator.api.AbstractRule;
import org.biopax.validator.api.beans.Validation;
import org.springframework.stereotype.Component;

/**
 * displayName length shouldn't exceed the specified limit.
 * @author rodche
 */
@Component
public class DisplayNameRule extends AbstractRule<Named> {
    public static final int MAX_DISPLAYNAME_LEN = 25;

	public boolean canCheck(Object thing) {
		return (thing instanceof Named); 
	}
    
    public void check(final Validation validation, Named named) 
    {
    	boolean fixed = false;
    	
    	if (named.getDisplayName() == null) {
    		if(validation.isFix()) {
    			// use the standardName if present
				if (named.getStandardName() != null) {
					named.setDisplayName(named.getStandardName());
					fixed = true;
				} // otherwise, use the shortest name, if anything...
				else if (!named.getName().isEmpty()) {
					String dsp = named.getName().iterator().next();
					for (String name : named.getName()) {
						if (name.length() < dsp.length())
							dsp = name;
					}
					named.setDisplayName(dsp);
					fixed = true;
				}
			}
    		// report
			error(validation, named, "no.display.name", fixed && validation.isFix());
		} 
    	
    	// check max. length
    	String name = named.getDisplayName();
    	if (name != null) { // if existed or was added above
        	Integer max = (named instanceof Provenance) ? 50 : MAX_DISPLAYNAME_LEN;
        	if (name.length() > max)
				error(validation, named, "too.long.display.name", false
					, name 
						+ ((fixed) ? "(auto-created form other names!)" : ""), name.length(), max);
		}
    }

}
