package org.biopax.validator.rules;

/*
 *
 */

import org.biopax.paxtools.controller.ShallowCopy;
import org.biopax.paxtools.model.level3.EntityFeature;
import org.biopax.paxtools.model.level3.EntityReference;
import org.biopax.paxtools.model.level3.SimplePhysicalEntity;
import org.biopax.paxtools.normalizer.Normalizer;
import org.biopax.validator.api.AbstractRule;
import org.biopax.validator.api.beans.Validation;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * A rule to check if a Simple Physical Entity Feature is also a feature of
 * its Entity Reference. If not, fixes it by adding the missing feature
 * to the Entity Reference.
 *
 */
@Component
public class SimplePhysicalEntityFeaturesRule extends AbstractRule<SimplePhysicalEntity>{

    public void check(final Validation validation, SimplePhysicalEntity thing) {
        EntityReference er = thing.getEntityReference();
        //sync: this er can belong to many SPEs, and this rule can be called from another thread for the other SPE
        synchronized (er) {	
        	Set<EntityFeature> erFeatures =  new HashSet<EntityFeature>(er.getEntityFeature());//defensive copy
        	
        	Set<EntityFeature> peFeaturesAndNotFeatures = new HashSet<EntityFeature>();
        	peFeaturesAndNotFeatures.addAll(thing.getFeature());
        	peFeaturesAndNotFeatures.addAll(thing.getNotFeature());

        	for(EntityFeature ef: peFeaturesAndNotFeatures) {
        		if(!erFeatures.contains(ef)) { //the ER does not have this EF...
        			if(validation.isFix()) {
        				if(ef.getEntityFeatureOf() != null) {//it belongs to the other ER
        					//trying to generate a new unique URI for the EF copy, for any ER,EF pair is unique 
        					//(considering one-to-many, inverse functional constraints of the entityFeature prop.):
        					String uri = Normalizer.uri(er.getUri() + "_", null, ef.getUri(), ef.getModelInterface());
        					EntityFeature newEf = null; //check if there is one with this URI already; use that one then
        					for(EntityFeature f : er.getEntityFeature()) {
        						if(uri.equals(f.getUri())) {
        							newEf = f;
        							break;
        						}
        					}
        					if(newEf == null) {
        						//make a copy
        						newEf = (new ShallowCopy()).copy(ef, uri);
        						assert newEf.getEntityFeatureOf() == null 
        							: "getEntityFeatureOf of a shallow copy of EF is not null...";
        					}
        					//replace the EF in the SPE's feature or notFeature set
        					if(thing.getFeature().contains(ef)) {
        						thing.removeFeature(ef);
        						thing.addFeature(newEf);
        					} 
        					if(thing.getNotFeature().contains(ef)) {
        						thing.removeNotFeature(ef);
        						thing.addNotFeature(newEf);
        					}
        					// add the new one to the ER
        					er.addEntityFeature(newEf);
        				} else {
        					// add the one to the ER
        					er.addEntityFeature(ef);
        				}
        			}

        			error(validation, thing, "improper.feature.use", validation.isFix(), ef.getUri(), er.getUri());
        		}
        	}        
        }             
    }

    public boolean canCheck(Object thing) {
        return (thing instanceof SimplePhysicalEntity) 
        		&& (((SimplePhysicalEntity) thing).getEntityReference() != null);
    }
}
