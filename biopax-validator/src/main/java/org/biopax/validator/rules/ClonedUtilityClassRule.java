package org.biopax.validator.rules;

import java.util.Set;

import org.biopax.paxtools.controller.AbstractTraverser;
import org.biopax.paxtools.controller.ObjectPropertyEditor;
import org.biopax.paxtools.controller.PropertyEditor;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.EntityReference;
import org.biopax.paxtools.model.level3.Named;
import org.biopax.paxtools.model.level3.UtilityClass;
import org.biopax.paxtools.model.level3.Xref;
import org.biopax.validator.api.AbstractRule;
import org.biopax.validator.api.beans.Validation;
import org.biopax.validator.utils.Cluster;
import org.biopax.paxtools.controller.SimpleEditorMap;
import org.springframework.stereotype.Component;

/**
 * Checks equivalent (duplicated) UtilityClass instances.
 * It collects/reports all duplicates. 
 * 
 * @author rodche
 */
@Component
public class ClonedUtilityClassRule extends	AbstractRule<Model> {
	
	public void check(final Validation validation, Model model) {
		Cluster<UtilityClass> algorithm = new Cluster<UtilityClass>() {
			@Override
			public boolean match(UtilityClass a, UtilityClass b) {
				return !a.equals(b) && a.isEquivalent(b);
			}
		};
		

		Set<Set<UtilityClass>> clusters 
			= algorithm.cluster(model.getObjects(UtilityClass.class), Integer.MAX_VALUE);
		
		// report the error once for each cluster
		for (Set<UtilityClass> clones : clusters) {
			if(clones.size() < 2)
				continue; //skip unique individuals
			
			UtilityClass first = clones.iterator().next();
			boolean ok = clones.remove(first); // pop the first element from the clones collection
			assert ok;
			if(validation.isFix()) {
				// use the same value for all corresp. props 
				fix(model, first, clones);
				// remove clones
				for (UtilityClass clone : clones) {
					model.remove(clone);
					if(logger.isDebugEnabled())
						logger.debug("Duplicate object " + clone.getRDFId() 
							+ " " + clone.getModelInterface().getSimpleName() 
							+ " has been replaced with " + first.getRDFId() +
							" and removed from the model");
				}
				
				// set "fixed", but keep the old message
				error(validation, first, "cloned.utility.class", 
					true, clones, first.getModelInterface().getSimpleName());
			} else {
				// report the problem (not fixed)
				error(validation, first, "cloned.utility.class", 
					false, clones, first.getModelInterface().getSimpleName());
			}
		}
	}

	public boolean canCheck(Object thing) {
		return thing instanceof Model 
			&& ((Model)thing).getLevel() == BioPAXLevel.L3;
	}

	
	private void fix(final Model model, final UtilityClass master, 
			final Set<UtilityClass> clones) {	
		if(master != null) {
			assert clones.size() > 0; 
			//- there was is at least one more (besides 'master') object
			
			@SuppressWarnings("unchecked") //- no filters
			AbstractTraverser traverser = 
				new AbstractTraverser(SimpleEditorMap.L3) 
			{	
				@Override
				protected void visit(Object range, BioPAXElement domain, 
						Model model, @SuppressWarnings("rawtypes") PropertyEditor editor) //TODO API issue...
				{		
					if(editor instanceof ObjectPropertyEditor && clones.contains(range)) 
					{
						if(!master.isEquivalent((UtilityClass)range)) {
							logger.error(master + " (" + master.getRDFId() 
								+ ", " + master.getModelInterface().getSimpleName()
								+ ") replaces NOT semantically equivalent " + 
								range + " (" + ((UtilityClass)range).getRDFId() 
								+ ", " + ((UtilityClass)range).getModelInterface().getSimpleName()
								+ ")! Ignored...");
						}
						
						// replace 'range' with 'master'
						if(editor.isMultipleCardinality())
							editor.removeValueFromBean(range, domain);
						editor.setValueToBean(master, domain);
						
						if(log.isDebugEnabled()) {
							log.debug("Replaced " 
								+ ((UtilityClass)range).getModelInterface().getSimpleName()
								+ " " + ((UtilityClass)range).getRDFId() + 
								" with " + master.getRDFId() +
								"; " + editor.toString() + "; (domain) bean: " + domain);
						}
					}
				}
			};
			
			/* for each biopax element, look 
			 * to update its object properties that
			 * refer to a duplicate (from 'clones') utility class
			 */
			for(BioPAXElement element : model.getObjects()) {
				traverser.traverse(element, model);
			}
			

			// merge comments and names to 'master'
			//TODO can do better? (smart/selective copying of data props, if required at all...)
			for(UtilityClass clone : clones) {
				for(String comm : clone.getComment())
					master.addComment(comm);
				if(master instanceof Named && clone instanceof Named) // - may be too much safety..
					for(String n : ((Named)clone).getName())
						((Named)master).addName(n);
				
				/* let's check for inconsistency, but
				 * keep (dangling) duplicates in the model,
				 * because, if we do remove them now, -
				 * won't be able to find other duplicate types later!
				 */
				if(clone instanceof Xref)
					assert(((Xref)clone).getXrefOf().isEmpty());
				else if(clone instanceof EntityReference)
					assert(((EntityReference)clone).getEntityReferenceOf().isEmpty());
			}
		}	
	}	
}
