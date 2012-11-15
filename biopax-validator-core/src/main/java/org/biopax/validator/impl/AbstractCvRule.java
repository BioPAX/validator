package org.biopax.validator.impl;

import java.util.*;

import javax.annotation.PostConstruct;
//import javax.annotation.Resource;

import org.biopax.paxtools.controller.PropertyEditor;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.validator.CvRule;
import org.biopax.validator.CvRestriction;
import org.biopax.validator.CvValidator;
import org.biopax.validator.impl.AbstractRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * An abstract class for CV terms checks.
 * 
 * @author rodche
 *
 * @param <D> property domain
 */
@Configurable
public abstract class AbstractCvRule<D extends BioPAXElement> extends AbstractRule<D> implements CvRule<D> {
    
	//@Resource // @Resource uses matching by name; too risky
    @Autowired
	protected CvValidator ontologyManager;
    
    protected final Class<D> domain;
    protected final String property; // helps validate generic ControlledVocabulary instances
    protected final Set<CvRestriction> restrictions;
	private Set<String> validTerms;
	protected PropertyEditor<? super D, ?> editor;
  
    /**
     * Constructor.
     * 
     * @param domain a BioPAX class for which the CV terms restrictions apply
     * @param property the name of the BioPAX property to get controlled vocabularies or null
     * @param restrictions a list of beans, each defining names (a subtree of an ontology) that 
     * is either to include or exclude (when 'not' flag is set) from the valid names set.
     */
    public AbstractCvRule(Class<D> domain, String property, CvRestriction... restrictions)
    {
    	this.domain = domain;
    	this.property = property;
        this.restrictions = new HashSet<CvRestriction>(restrictions.length);
    	for(CvRestriction c: restrictions) {
        	this.restrictions.add(c);
        }    	
    }
	
    @PostConstruct
    public void init() {
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
	public Set<CvRestriction> getRestrictions() {
		return restrictions;
	}
	
	/* (non-Javadoc)
	 * @see org.biopax.validator.impl.CvRule#getDomain()
	 */
	public Class<D> getDomain() {
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
	public PropertyEditor<? super D, ?> getEditor() {
		return editor;
	}
}
