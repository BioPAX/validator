package org.biopax.validator.impl;

import java.util.*;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.biopax.paxtools.controller.EditorMap;
import org.biopax.paxtools.model.level2.Level2Element;
import org.biopax.paxtools.model.level2.openControlledVocabulary;
import org.biopax.validator.utils.BiopaxValidatorUtils;
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
public abstract class Level2CvTermsRule<T extends Level2Element> 
	extends AbstractCvRule<T> {
	
    @Resource
    private EditorMap editorMap2;
    
    public Level2CvTermsRule(Class<T> domain, String property,  CvTermRestriction... restrictions)
    {
    	super(domain, property, restrictions);
    }

    @PostConstruct
    public void init() {
    	super.init();
		this.editor = (property != null && !openControlledVocabulary.class.isAssignableFrom(domain)) 
			? editorMap2.getEditorForProperty(property, this.domain)
			: null;    	
    };
    
	public void check(T thing, boolean fix) {
		// a set of CVs for this rule to validate
		Collection<openControlledVocabulary> vocabularies = new HashSet<openControlledVocabulary>();
		// if the editor is null, we expect a CV object
		if(editor == null) {
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
					if(!getValidTerms().contains(name.toLowerCase())) {
						error(thing, "illegal.cv.term", name, BiopaxValidatorUtils.getLocalId(cv),
							((editor!=null)? " at element's property: " + property : "") 
							+ " did not meet the following criteria: " 
							+ restrictions.toString());
					}
				}
			}			
		}
		
	}       
}
