package org.biopax.validator.rules;

import org.biopax.paxtools.controller.ShallowCopy;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.EntityFeature;
import org.biopax.paxtools.model.level3.EntityReference;
import org.biopax.paxtools.model.level3.SimplePhysicalEntity;
import org.biopax.validator.api.AbstractRule;
import org.biopax.validator.api.beans.Validation;
import org.biopax.validator.utils.Normalizer;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * A rule that can check and fix the error cases when an EntityFeature 
 * is owned by multiple EntityReferences, which violates the entityFeature 
 * property's 'inverse functional' BioPAX L3 OWL constraint.
 *
 */
@Component
public class EntityFeatureInverseFunctionalRule extends AbstractRule<Model>{

    public void check(final Validation validation, Model model) {
       	Set<EntityFeature> efs =  new HashSet<EntityFeature>(model.getObjects(EntityFeature.class));//defensive copy
       	Set<EntityReference> ers =  new HashSet<EntityReference>(model.getObjects(EntityReference.class));
       	
       	for(EntityFeature ef: efs) {           		
       		EntityReference efOf = ef.getEntityFeatureOf();	//the last assigned or the only owner ER of this EF
       		//if the model does not contain the efOf object, no worries, we'll check this rule anyway  		      		
       		Set<EntityReference> ownerEntityRefs = new HashSet<EntityReference>();
       		for(EntityReference er : ers) {
       			if(!er.getEntityFeature().contains(ef))
       				continue; //skip
       			
       			ownerEntityRefs.add(er); //owner ER found!
       			
       			if(er.equals(efOf)) {
       				continue; //no fix required for this one
       			}
       			
       			if(validation.isFix()) {     		    	
   		    		//do fix (make a new EF copy, replace in the properties)
   		    		String newUri = Normalizer.uri(er.getRDFId() + "_", null, ef.getRDFId(), ef.getModelInterface());
   		    		EntityFeature newEf = (new ShallowCopy()).copy(ef, newUri);
   		    		model.add(newEf);
   		    		er.removeEntityFeature(ef);
   		    		er.addEntityFeature(newEf);
       		    	for(SimplePhysicalEntity spe : er.getEntityReferenceOf()) {
       		    		//replace the ef with newEf in the ER and in all owner SPEs
       		    		if(spe.getFeature().contains(ef)) {
       		    			spe.removeFeature(ef);
       		    			spe.addFeature(newEf);
       		    		} 
       		    		if(spe.getNotFeature().contains(ef)) {
       		    			spe.removeNotFeature(ef);
       		    			spe.addNotFeature(newEf);
       		    		}
       		    	}	    		
       			}
       		}
       		
       		//if ersToFix.isEmpty()==true - dangling EF - the other rule can fix (if this EF belongs to a PE)
       		
       		if(ownerEntityRefs.size()>1) {
       			error(validation, ef, "inverse.functional.violated", validation.isFix(), "entityFeature", ef, ownerEntityRefs.toString());	
       		} 
       		else if(ownerEntityRefs.size()==1) {  
       			EntityReference er = ownerEntityRefs.iterator().next();
       			if(efOf == null || !efOf.equals(er)) { 
       				//provided ersToFix.size()==1, this here should never happen unless Paxtools API's hacked, or the model's incomplete...
       				if(validation.isFix()) {
       					//easy and quiet fix (er.entityFeature has ef, whereas ef.entityFeatureOf() is null)
       					er.removeEntityFeature(ef);
       					er.addEntityFeature(ef);
       				}
       				
       				if(efOf!=null)
       					error(validation, efOf, "inverse.functional.violated", validation.isFix(), "entityFeature", ef, er);	
       			}
       		}
       	}
    }

	public boolean canCheck(Object thing) {
		return thing instanceof Model;
	}
    
}
