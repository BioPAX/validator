package org.biopax.validator.rules;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections15.set.CompositeSet;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.BindingFeature;
import org.biopax.validator.impl.AbstractRule;
import org.biopax.validator.utils.BiopaxValidatorUtils;
import org.biopax.validator.utils.Cluster;
import org.springframework.stereotype.Component;

/**
 * Checks:
 * BindingFeature.bindsTo is 'inverse functional':
 * 
 * @author rodche
 */
@Component
public class BindingFeatureExtraRules extends AbstractRule<Model> {

	public boolean canCheck(Object thing) {
		return thing instanceof Model 
			&& ((Model)thing).getLevel() == BioPAXLevel.L3;
	}

	public void check(Model model, boolean fix) {
		Set<BindingFeature> bfs = new HashSet<BindingFeature>(
				model.getObjects(BindingFeature.class));

		Cluster<BindingFeature> groupping = new Cluster<BindingFeature>() {
			@Override
			public boolean match(BindingFeature a, BindingFeature b) {
				boolean ab = a.getBindsTo() != null 
					&& b.getBindsTo() != null
						&& a.getBindsTo().isEquivalent(b.getBindsTo());
				
				return !a.isEquivalent(b) && ab;
			}
		};
		
		CompositeSet<BindingFeature> violations 
			= groupping.groupByEquivalence(bfs.toArray(new BindingFeature[]{}),
					BiopaxValidatorUtils.maxErrors);
		
		// report the error once for each cluster
		for (Collection<BindingFeature> s : violations.getCollections()) {
			BindingFeature a = s.iterator().next();
			error(a, "inverse.functional.violated", false,	"bindsTo", 
					a.getBindsTo(), BiopaxValidatorUtils.getIdListAsString(s));
		}

	}
   
}
