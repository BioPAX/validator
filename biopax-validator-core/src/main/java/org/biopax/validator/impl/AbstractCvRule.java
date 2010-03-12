package org.biopax.validator.impl;

import java.util.*;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.biopax.paxtools.controller.PropertyEditor;
import org.biopax.validator.utils.OntologyManagerAdapter;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * An abstract class for CV terms checks.
 * 
 * @author rodch
 * 
 */
@Configurable
public abstract class AbstractCvRule<T> extends AbstractRule<T> {
    
	@Resource
    protected OntologyManagerAdapter ontologyManager;
    
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
    		if(logger.isInfoEnabled()) {
    			logger.info("rule created but  (ontologyManager=null)");
    		}
    	}
    };
    
    
	public boolean canCheck(Object thing) {
		return domain.isInstance(thing);
	}
	
	/**
	 * Gets valid CV terms (that obey the restrictions)
	 * @return
	 */
	public Set<String> getValidTerms() {
		return validTerms;
	}
	
	/**
	 * Sets valid terms for this CV rule.
	 * @param validTerms
	 */
	public void setValidTerms(Set<String> validTerms) {
		this.validTerms = validTerms;
	}
	
	// for unit testing

	public Set<CvTermRestriction> getRestrictions() {
		return restrictions;
	}
	
	public Class<T> getDomain() {
		return domain;
	}
	
	public String getProperty() {
		return property;
	}
	
	/**
	 * Gets the internal OntologyManagerAdapter instance
	 * @return
	 */
	public OntologyManagerAdapter getOntologyManagerAdapter() {
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
