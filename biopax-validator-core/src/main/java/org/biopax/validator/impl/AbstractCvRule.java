package org.biopax.validator.impl;

import java.util.*;

import javax.annotation.PostConstruct;
//import javax.annotation.Resource;

import org.biopax.paxtools.controller.PropertyEditor;
import org.biopax.validator.CvRule;
import org.biopax.validator.utils.CvValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * An abstract class for CV terms checks.
 * 
 * @author rodch
 * 
 */
@Configurable
public abstract class AbstractCvRule<T> extends AbstractRule<T> implements CvRule<T> {
    
	//@Resource // matching by name (can be risky..)
    @Autowired
	protected CvValidator ontologyManager;
    
    protected final Class<T> domain;
    protected final String property; // helps validate generic ControlledVocabulary instances
    protected final Set<CvTermRestriction> restrictions;
	private Set<String> validTerms;
	protected PropertyEditor editor;
  
    /**
     * Constructor.
     * 
     * @param domain a BioPAX class for which the CV terms restrictions apply
     * @param property the name of the BioPAX property to get controlled vocabularies or null
     * @param restrictions a list of beans, each defining names (a subtree of an ontology) that 
     * is either to include or exclude (when 'not' flag is set) from the valid names set.
     */
    public AbstractCvRule(Class<T> domain, String property, CvTermRestriction... restrictions)
    {
    	this.domain = domain;
    	this.property = property;
        this.restrictions = new HashSet<CvTermRestriction>(restrictions.length);
    	for(CvTermRestriction c: restrictions) {
        	this.restrictions.add(c);
        }    	
    }
	
    @PostConstruct
    public void init() {
    	super.init();
    	if(ontologyManager != null) {
    		setValidTerms(ontologyManager.getValidTermNames(this));
    	} else {
    		throw new IllegalStateException("ontologyManager is NULL!");
    	}
    };
    
    
	public boolean canCheck(Object thing) {
		return domain.isInstance(thing);
	}
	
	/* (non-Javadoc)
	 * @see org.biopax.validator.impl.CvRule#getValidTerms()
	 */
	public Set<String> getValidTerms() {
		return validTerms;
	}
	
	/* (non-Javadoc)
	 * @see org.biopax.validator.impl.CvRule#setValidTerms(java.util.Set)
	 */
	public void setValidTerms(Set<String> validTerms) {
		this.validTerms = validTerms;
	}
	
	// for unit testing

	/* (non-Javadoc)
	 * @see org.biopax.validator.impl.CvRule#getRestrictions()
	 */
	public Set<CvTermRestriction> getRestrictions() {
		return restrictions;
	}
	
	/* (non-Javadoc)
	 * @see org.biopax.validator.impl.CvRule#getDomain()
	 */
	public Class<T> getDomain() {
		return domain;
	}
	
	/* (non-Javadoc)
	 * @see org.biopax.validator.impl.CvRule#getProperty()
	 */
	public String getProperty() {
		return property;
	}
	
	/**
	 * Gets the internal BiopaxOntologyManager instance
	 * @return
	 */
	public CvValidator getBiopaxOntologyManager() {
		return ontologyManager;
	}
	
	/**
	 * Gets the corresponding CV property editor.
	 * Returns null if either the 'domain' itself is of CV type
	 * or the 'property' is null.
	 * 
	 * @return
	 */
	public PropertyEditor getEditor() {
		return editor;
	}
}
