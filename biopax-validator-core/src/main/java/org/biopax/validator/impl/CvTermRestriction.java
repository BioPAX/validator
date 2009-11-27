package org.biopax.validator.impl;

import java.io.Serializable;

/**
 * @author rodch
 */
public class CvTermRestriction implements Serializable {

	private static final long serialVersionUID = 1L;
	private final String id; //e.g., "GO:0005575"
    private final String ontologyId; // "GO"
	private final boolean termAllowed;
    private final UseChildTerms childrenAllowed;
    private final boolean not;
	
    public CvTermRestriction() {
    	this(null, null, false, null, false);
	}
    
	public CvTermRestriction(String id, String ontologyId, boolean useThisTerm, 
			UseChildTerms useChildTerms, boolean isNot) {
		this.id = id;
		this.ontologyId = ontologyId;
		this.termAllowed = useThisTerm;
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
		return termAllowed;
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
			+ ((termAllowed)? id + " and " : "") 
			+ childrenAllowed.toString() + " children of " + id;
	}
	
	public enum UseChildTerms {
		ALL, DIRECT, NONE;
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		return 
			obj instanceof CvTermRestriction 
				&& toString().equals(obj.toString());
	}
}
