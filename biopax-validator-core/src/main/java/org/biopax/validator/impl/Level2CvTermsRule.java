package org.biopax.validator.impl;

import java.util.*;

import javax.annotation.Resource;

import org.biopax.paxtools.controller.EditorMap;
import org.biopax.paxtools.controller.PropertyEditor;
import org.biopax.paxtools.model.level2.Level2Element;
import org.biopax.paxtools.model.level2.openControlledVocabulary;
import org.biopax.validator.utils.BiopaxValidatorUtils;
import org.biopax.validator.utils.OntologyUtils;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * An abstract class for CV terms checking (BioPAX Level2).
 * 
 * @author rodch
 * 
 * TODO implement simple traversing of properties, e.g., accept "propertyA/propertyB"
 * 
 */
@Configurable
public abstract class Level2CvTermsRule<T extends Level2Element> extends AbstractRule<T> {
    @Resource
    protected OntologyUtils ontologyManager;
    
    @Resource
    private EditorMap editorMap2;
    
    private final Class<T> domain;
    private final String property; // helps validate generic ControlledVocabulary instances
    private final Set<String> allowedTerms;
    private final Set<CvTermRestriction> restrictions;
    

    /**
     * Constructor.
     * 
     * @param domain a BioPAX class for which the CV terms restrictions apply
     * @param property the name of the BioPAX property to get controlled vocabularies or null
     * @param restrictions a list of beans, each defining names (a subtree of an ontology) that 
     * is either to include or exclude (when 'not' flag is set) from the valid names set.
     */
    public Level2CvTermsRule(Class<T> domain, String property,  CvTermRestriction... restrictions)
    {
    	this.domain = domain;
    	this.property = property;
        this.allowedTerms = new HashSet<String>();
        this.restrictions = new HashSet<CvTermRestriction>(restrictions.length);
    	for(CvTermRestriction c: restrictions) {
        	this.restrictions.add(c);
        }
    }

	public void check(T thing) {
		// a set of CVs for this rule to validate
		Collection<openControlledVocabulary> vocabularies = new HashSet<openControlledVocabulary>();
		PropertyEditor editor = (property != null) 
		? editorMap2.getEditorForProperty(property, ((Level2Element)thing).getModelInterface())
		: null;
		// if the 'property' is not set, we expect a CV object
		if(editor == null) {
			// throws ClassCastException if the rule is malformed 
			// (should either use correct property or ControlledVocabulary class)!
			vocabularies.add((openControlledVocabulary)thing); 
		} else if(editor.isMultipleCardinality()) {
			vocabularies = (Collection<openControlledVocabulary>) editor.getValueFromBean(thing);
		} else {
			openControlledVocabulary value = (openControlledVocabulary) editor.getValueFromBean(thing);
			if(value != null) vocabularies.add(value);
		}
		
		// shortcut
		if(vocabularies.isEmpty()) return;
		
		// check each CV terms against the restrictions
		for (openControlledVocabulary cv : vocabularies) {
			if (cv == null) {
				logger.warn(thing
						+ " referes to 'null' controlled vocabulary (bug!): "
						+ ", domain: " + domain + ", property: " + property);
			} else if(cv.getTERM().isEmpty()) {
				// another rule should report this...
			} else {
				for(String name : cv.getTERM()) {
					if(!allowedTerms.contains(name.toLowerCase())) {
						error(thing, "illegal.cv.term", name, BiopaxValidatorUtils.getLocalId(cv),
							((editor!=null)? " at element's property: " + property : "") 
							+ " did not meet the following criteria: " 
							+ restrictions.toString());
					}
				}
			}			
		}
		
	}       
	
	protected void fix(T t, Object... values) {
		// TODO Auto-generated method stub	
	}

	
	public boolean canCheck(Object thing) {
		return domain.isInstance(thing);
	}
	
	
	// for unit testing
	
	public Set<String> getAllowedTerms() {
		return allowedTerms;
	}
	
	public Set<CvTermRestriction> getRestrictions() {
		return restrictions;
	}
	
	public Class<T> getDomain() {
		return domain;
	}
	
	public String getProperty() {
		return property;
	}
}
