package org.biopax.validator.rules;

import org.biopax.paxtools.model.level2.physicalEntityParticipant;
import org.biopax.paxtools.model.level2.physicalEntity;
import org.biopax.validator.impl.Level2CardinalityAndRangeRule;
import org.springframework.stereotype.Component;

/**
 * Checks:
 * physicalEntityParticipant.PHYSICAL-ENTITY cardinality/range.
 * @author rodche
 */
@Component
public class Level2PhysicalEntityParticipantPhysicalEntityCRRule 
	extends Level2CardinalityAndRangeRule<physicalEntityParticipant> {
	
	public Level2PhysicalEntityParticipantPhysicalEntityCRRule() {
		super(physicalEntityParticipant.class, "PHYSICAL-ENTITY", 
				1, 1, physicalEntity.class);
	}
}
