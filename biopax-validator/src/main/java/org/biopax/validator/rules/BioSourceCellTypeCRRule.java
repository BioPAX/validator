package org.biopax.validator.rules;


import org.biopax.paxtools.model.level3.BioSource;
import org.biopax.paxtools.model.level3.CellVocabulary;
import org.biopax.validator.CardinalityAndRangeRule;
import org.springframework.stereotype.Component;

/**
 * Checks BioSource.cellType cardinality/range constraint: the property has none or one CellVocabulary value.
 * @author rodche
 */
@Component
public class BioSourceCellTypeCRRule extends CardinalityAndRangeRule<BioSource> {
	public BioSourceCellTypeCRRule() {
		super(BioSource.class, "cellType", 0, 1, CellVocabulary.class);
	}
}
