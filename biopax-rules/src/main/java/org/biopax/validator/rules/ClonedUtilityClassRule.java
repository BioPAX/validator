package org.biopax.validator.rules;

import java.util.Collection;

import org.apache.commons.collections15.set.CompositeSet;
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
import org.biopax.validator.impl.AbstractRule;
import org.biopax.paxtools.controller.SimpleEditorMap;
import org.biopax.validator.utils.BiopaxValidatorUtils;
import org.biopax.validator.utils.Cluster;
import org.springframework.stereotype.Component;

/**
 * Checks equivalent (duplicated) UtilityClass instances.
 * It collects/reports up to {@link BiopaxValidatorUtils#maxErrors}
 * duplicates (perhaps, not all). 
 * 
 * @author rodche
 */
@Component
public class ClonedUtilityClassRule extends	AbstractRule<Model> {
	
	public void check(Model model, boolean fix) {
		UtilityClass[] peers = 
				model.getObjects(UtilityClass.class).toArray(new UtilityClass[]{});
		
		Cluster<UtilityClass> algorithm = new Cluster<UtilityClass>() {
			@Override
			public boolean match(UtilityClass a, UtilityClass b) {
				return !a.equals(b) && a.isEquivalent(b);
			}
		};
		
		/* Note: BiopaxValidatorUtils.maxErrors (e.g., 50) here sets
		 * the max. no. of duplicates to report (the rest is ignored).
		 * TODO report/fix all the duplicates (performance risk)...
		 */
		CompositeSet<UtilityClass> clasters 
			= algorithm.groupByEquivalence(peers, BiopaxValidatorUtils.maxErrors);
		
		// report the error once for each cluster
		for (Collection<UtilityClass> clones : clasters.getCollections()) {
			UtilityClass u = clones.iterator().next();
			clones.remove(u); // keep the first element
			String idListAsString = BiopaxValidatorUtils.getIdListAsString(clones);
			if(fix) {
				// use the same value for all corresp. props 
				fix(model, u, clones);
				// set "fixed", but keep the old message
				error(u, "cloned.utility.class", true, 
					idListAsString, u.getModelInterface().getSimpleName());
			} else {
				// report the problem (not fixed)
				error(u, "cloned.utility.class", false, 
					idListAsString, u.getModelInterface().getSimpleName());
			}
		}
		
		// now should be safe to remove them from the model
		if (fix) {
			for (UtilityClass duplicate : clasters.toCollection()) {
				model.remove(duplicate);
				if(logger.isInfoEnabled())
					logger.info("Duplicate object " + duplicate.getRDFId() 
						+ " " + duplicate.getModelInterface().getSimpleName() 
						+ " has been removed from the model"
						+ " and all object properties updated!");
			}
		}
	}

	public boolean canCheck(Object thing) {
		return thing instanceof Model 
			&& ((Model)thing).getLevel() == BioPAXLevel.L3;
	}

	
	private void fix(final Model model, final UtilityClass master, 
			final Collection<UtilityClass> clones) {	
		if(master != null) {
			AbstractTraverser traverser = 
				new AbstractTraverser(SimpleEditorMap.L3) 
			{	
				@Override
				protected void visit(Object range, BioPAXElement domain, 
						Model model, PropertyEditor editor) 
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
