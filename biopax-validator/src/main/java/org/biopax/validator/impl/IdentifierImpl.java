/**
 * 
 */
package org.biopax.validator.impl;

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

import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.validator.api.beans.Validation.Identifier;

/**
 * BioPAX object domain specific implementation 
 * of the {@link Identifier} strategy.
 * 
 * @author rodche
 *
 */
public final class IdentifierImpl implements Identifier {

	/* (non-Javadoc)
	 * @see org.biopax.validator.api.Identifier#getId(java.lang.Object)
	 */
	public String identify(Object obj) {
    	String id = "";
    	
		if(obj instanceof SimpleIOHandler) {
			SimpleIOHandler r = (SimpleIOHandler) obj;
			id = r.getClass().getSimpleName(); 
	    	try {
	    		id = r.getId(); //current element URI
	    	} catch (Throwable e) {
	    		id = r.getXmlStreamInfo(); //location
			}
		} else if (obj instanceof BioPAXElement 
				&& ((BioPAXElement)obj).getRDFId() != null) {
			id = ((BioPAXElement) obj).getRDFId().replaceFirst("^.+#", "");	
			// - strictly spk., does not always get the local part (depends on xml:base) but is OK.
		} else if(obj instanceof Model) {
			id = obj + "; xml:base=" + ((Model) obj).getXmlBase();
		} else {
			id = "" + obj;
		}
		
		return id;
	}
}
