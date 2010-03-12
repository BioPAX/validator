package org.biopax.validator.rules;

import org.biopax.paxtools.model.level3.EntityReference;
import org.biopax.paxtools.model.level3.BioSource;
import org.biopax.paxtools.model.level3.SmallMoleculeReference;
import org.biopax.validator.impl.Level3CardinalityAndRangeRule;
import org.springframework.stereotype.Component;

/**
 * Checks:
 * EntityReference.organism (except for SmallMoleculeReference) cardinality/range.
 * @author rodche
 */
@Component
public class EntityReferenceOrganismCRRule extends Level3CardinalityAndRangeRule<EntityReference> {
	public EntityReferenceOrganismCRRule() {
		super(EntityReference.class, "organism", 0, 1, BioSource.class);
		/**
		 * To tell the truth, EntityReference does not have property 'organism', but its 
		 * subclasses do. There is no problem, as the value is called for at runtime from the 
		 * BioPAX object (thing) passed to the 'check' method.
		 */
		
	}
	
	@Override
	public boolean canCheck(Object thing) {
		return super.canCheck(thing) && !(thing instanceof SmallMoleculeReference);
	}
}
