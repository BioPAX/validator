package org.biopax.validator.api;

/*
 * #%L
 * Object Model Validator Core
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

/**
 * @author rodch
 */
public final class CvRestriction {
	
	private final String id; //e.g., "GO:0005575"
    private final String ontologyId; // "GO"
	private final boolean allowed;
    private final UseChildTerms childrenAllowed;
    private final boolean not;
	
	public CvRestriction(String id, String ontologyId, boolean useThisTerm, 
			UseChildTerms useChildTerms, boolean isNot) {
		this.id = id;
		this.ontologyId = ontologyId;
		this.allowed = useThisTerm;
		this.childrenAllowed = useChildTerms;
		this.not = isNot;
	}
	
	public String getId() {
		return id;
	}

	public String getOntologyId() {
		return ontologyId;
	}

	public boolean isTermAllowed() {
		return allowed;
	}

	
	public UseChildTerms getChildrenAllowed() {
		return childrenAllowed;
	}
	
	public boolean isNot() {
		return not;
	}
	
	@Override
	public String toString() {
		return ((not)? "NOT allowed " : "Valid '") 
			+ ontologyId + "' terms: " 
			+ ((allowed)? id + " and " : "") 
			+ childrenAllowed.toString() + " children of " + id;
	}
	
	public enum UseChildTerms {
		ALL, DIRECT, NONE;
	}
}
