package org.biopax.validator.impl;

import java.util.*;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.biopax.paxtools.controller.EditorMap;
import org.biopax.paxtools.model.level3.Level3Element;
import org.biopax.paxtools.model.level3.ControlledVocabulary;
import org.biopax.validator.utils.BiopaxValidatorUtils;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * An abstract class for CV terms checking (BioPAX Level3).
 * 
 * @author rodch
 * 
 * TODO implement simple traversing of properties, e.g., accept "modificationFeature/modificationType"
 */
@Configurable
public abstract class CvTermsRule<T extends Level3Element> extends AbstractCvRule<T> {
    
    @Resource
    private EditorMap editorMap3;
  
    public CvTermsRule(Class<T> domain, String property, CvTermRestriction... restrictions)
    {
    	super(domain, property, restrictions);
    }
   
    @PostConstruct
    public void init() {
    	super.init();
		this.editor = (property != null && !ControlledVocabulary.class.isAssignableFrom(domain)) 
			? editorMap3.getEditorForProperty(property, this.domain)
			: null;    	
    };
    
    
	public void check(T thing) {
		// a set of CVs for this rule to validate
		Collection<ControlledVocabulary> vocabularies = new HashSet<ControlledVocabulary>();
		// if the editor is null, we expect a ControlledVocabulary object!
		if(editor == null) {
			vocabularies.add((ControlledVocabulary)thing); 
		} else if(editor.isMultipleCardinality()) {
			vocabularies = (Collection<ControlledVocabulary>) editor.getValueFromBean(thing);
		} else {
			ControlledVocabulary value = (ControlledVocabulary) editor.getValueFromBean(thing);
			if(value != null) vocabularies.add(value);
		}
		
		// shortcut
		if(vocabularies.isEmpty()) return;
		
		// check each CV terms against the restrictions
		for (ControlledVocabulary cv : vocabularies) {
			if (cv == null) {
				logger.warn(thing
						+ " referes to 'null' controlled vocabulary (bug!): "
						+ ", domain: " + domain + ", property: " + property);
			} else if(cv.getTerm().isEmpty()) {
				// another rule should report this...
			} else {
				for(String name : cv.getTerm()) {
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
