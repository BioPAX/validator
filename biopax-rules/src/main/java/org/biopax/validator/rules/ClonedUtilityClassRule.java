package org.biopax.validator.rules;

import java.util.Collection;
import java.util.Set;

import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.UtilityClass;
import org.biopax.validator.impl.AbstractRule;
import org.biopax.validator.utils.BiopaxValidatorUtils;
import org.biopax.validator.utils.Cluster;
import org.springframework.stereotype.Component;

/**
 * Checks equivalent (duplicated) UtilityClass instances
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
		
		Collection<Set<UtilityClass>> clones = algorithm.groupByEquivalence(peers,
				BiopaxValidatorUtils.maxErrors);
		
		// report the error once for each cluster
		for (Set<UtilityClass> s : clones) {
			UtilityClass u = s.iterator().next();
			error(u, "cloned.utility.class", 
				BiopaxValidatorUtils.getIdListAsString(s), 
				u.getModelInterface().getSimpleName());
		}
	}

	public boolean canCheck(Object thing) {
		return thing instanceof Model && ((Model)thing).getLevel() == BioPAXLevel.L3;
	}

}
